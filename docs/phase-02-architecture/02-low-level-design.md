# LexMind AI — Low-Level Design (LLD)

**Document:** Phase 2 / 02
**Status:** Draft for review
**Owner:** Solution Architecture / Backend
**Last updated:** 2026-06-14

> Detailed design of the application tier: package structure, domain model classes, key
> service interfaces, and sequence diagrams for the critical flows. Implementation lands in
> Phase 4 (backend) and Phase 6 (AI).

---

## 1. Backend Package Structure (Spring Boot, Java 21)

Feature-oriented packaging (a package per bounded context), each with the standard layering.

```
com.lexmind
├── LexMindApplication.java
├── common/                     # cross-cutting
│   ├── config/                 # SecurityConfig, OpenApiConfig, AsyncConfig, CorsConfig
│   ├── security/               # JwtService, JwtAuthFilter, RbacEvaluator, UserPrincipal
│   ├── audit/                  # @Audited, AuditAspect, AuditService
│   ├── error/                  # ApiError, GlobalExceptionHandler, exceptions
│   ├── ratelimit/              # RateLimitFilter (bucket4j)
│   ├── dto/                    # PageResponse, ApiResponse envelope
│   └── util/                   # mappers, validators
├── auth/                       # register/login/refresh/reset
│   ├── api/  (AuthController, dto/)
│   ├── domain/ (User, Role, Permission, RefreshToken, PasswordResetToken)
│   ├── repo/   (UserRepository, RoleRepository, ...)
│   └── service/ (AuthService, TokenService, UserService)
├── organization/               # firm tenancy + seats
├── casefile/                   # cases (matters), parties, members
│   ├── api/ domain/ repo/ service/
├── document/                   # upload, storage, status, download
│   ├── api/ domain/ repo/ service/
│   └── storage/ (StorageService → LocalStorage | S3Storage)
├── analysis/                   # analysis runs + agent executions + result persistence
│   ├── api/ domain/ repo/ service/
│   └── client/ (AiServiceClient — WebClient)
├── intelligence/               # read-side: facts, timeline, issues, statutes, args,
│   │                           # evidence, witnesses, precedents, irac, brief
│   ├── api/ (DashboardController) domain/ repo/ service/
├── analytics/                  # strength, risk, readiness, trends
├── chat/                       # RAG chat sessions/messages
├── research/                   # citation, similar-case, notes
└── admin/                      # user mgmt, audit read, AI/doc monitoring
```

**Conventions**
- Controllers accept/return **DTOs** only; MapStruct (or manual mappers) convert ↔ entities.
- Services hold transactions (`@Transactional`) and business rules; repositories are thin.
- RBAC via `@PreAuthorize("hasAuthority('case:read')")` + a tenant/owner check in service.
- All mutating endpoints carry `@Audited`.

---

## 2. Domain Model — Class Diagram (core)

```mermaid
classDiagram
    class User {
        +UUID id
        +String email
        +String passwordHash
        +String fullName
        +Role role
        +UUID organizationId
        +UserStatus status
        +boolean emailVerified
        +Instant createdAt
    }
    class Role {
        +UUID id
        +RoleName name
        +Set~Permission~ permissions
    }
    class Permission {
        +UUID id
        +String code  // e.g. "case:read"
    }
    class Organization {
        +UUID id
        +String name
        +OrgType type
        +Plan plan
        +int seatLimit
    }
    class CaseFile {
        +UUID id
        +UUID ownerId
        +UUID organizationId
        +String title
        +String caseNumber
        +String court
        +String jurisdiction
        +CaseType caseType
        +CaseStage stage
        +LocalDate filingDate
        +CaseStatus status
    }
    class CaseParty {
        +UUID id
        +UUID caseId
        +String name
        +PartySide side  // PETITIONER/RESPONDENT/...
        +String counsel
    }
    class Document {
        +UUID id
        +UUID caseId
        +UUID uploadedBy
        +String originalFilename
        +String storageKey
        +String mimeType
        +long sizeBytes
        +DocType docType
        +DocStatus status
        +int pageCount
        +boolean ocrApplied
        +String checksum
    }
    class AnalysisRun {
        +UUID id
        +UUID caseId
        +UUID triggeredBy
        +RunStatus status
        +String model
        +long totalTokens
        +BigDecimal costUsd
        +Instant startedAt
        +Instant completedAt
    }
    class AgentExecution {
        +UUID id
        +UUID analysisRunId
        +AgentType agentType
        +RunStatus status
        +int latencyMs
        +long tokens
    }
    class IracAnalysis {
        +UUID id
        +UUID caseId
        +String issue
        +String rule
        +String application
        +String conclusion
    }
    class AuditLog {
        +UUID id
        +UUID actorUserId
        +String action
        +String resourceType
        +UUID resourceId
        +Instant createdAt
    }

    Organization "1" o-- "many" User
    Role "1" o-- "many" User
    Role "many" -- "many" Permission
    User "1" o-- "many" CaseFile : owns
    Organization "1" o-- "many" CaseFile
    CaseFile "1" *-- "many" CaseParty
    CaseFile "1" *-- "many" Document
    CaseFile "1" *-- "many" AnalysisRun
    AnalysisRun "1" *-- "many" AgentExecution
    CaseFile "1" *-- "many" IracAnalysis
```

The full set of read-side intelligence entities (facts, timeline events, issues, statutes,
arguments, evidence, witnesses, precedents, briefs, analytics) is detailed in
[03-database-design.md](03-database-design.md).

---

## 3. Key Service Interfaces (contracts)

```java
public interface AnalysisService {
    /** Create a run, enqueue async processing, return run id immediately. */
    AnalysisRunDto startAnalysis(UUID caseId, AnalysisOptions opts, UserPrincipal actor);

    AnalysisRunDto getRun(UUID runId, UserPrincipal actor);

    /** Called by the AI tier (internal, authenticated) to persist structured results. */
    void ingestResults(UUID runId, AgentResultsPayload payload);
}

public interface StorageService {
    StoredObject store(UUID caseId, MultipartFile file);   // LocalStorage | S3Storage
    Resource load(String storageKey);
    void delete(String storageKey);
}

public interface AiServiceClient {                          // WebClient → FastAPI
    Mono<ProcessAck> processDocument(ProcessDocRequest req);
    Mono<AgentResultsPayload> runAgents(RunAgentsRequest req);   // or async w/ callback
    Mono<ChatAnswer> chat(ChatRequest req);                 // RAG, case-scoped
}

public interface RbacEvaluator {
    boolean canAccessCase(UserPrincipal actor, UUID caseId, Action action);
}
```

---

## 4. Sequence Diagrams (critical flows)

### 4.1 Authentication (login + JWT)

```mermaid
sequenceDiagram
    actor U as User
    participant FE as SPA
    participant API as AuthController
    participant SVC as AuthService
    participant DB as PostgreSQL
    U->>FE: enter email/password
    FE->>API: POST /api/v1/auth/login
    API->>SVC: authenticate(creds)
    SVC->>DB: find user by email
    DB-->>SVC: user + role + permissions
    SVC->>SVC: verify password (BCrypt)
    SVC->>SVC: issue access JWT (15m) + refresh token (persisted)
    SVC-->>API: tokens + profile
    API-->>FE: 200 {accessToken, refreshToken, user}
    FE->>FE: store tokens, route by role
```

### 4.2 Document upload → async analysis

```mermaid
sequenceDiagram
    actor U as Advocate
    participant FE as SPA
    participant API as Document/Analysis API
    participant ST as StorageService
    participant DB as PostgreSQL
    participant Q as Job Queue
    participant AI as AI Service
    participant QD as Qdrant

    U->>FE: upload PDF(s) to case
    FE->>API: POST /cases/{id}/documents (multipart)
    API->>API: validate (type, size, magic bytes, AV)
    API->>ST: store(file) → storageKey
    API->>DB: insert document(status=QUEUED)
    API-->>FE: 201 {documentId, status:QUEUED}

    FE->>API: POST /cases/{id}/analyze
    API->>DB: insert analysis_run(status=QUEUED)
    API->>Q: enqueue(runId, caseId, docIds)
    API-->>FE: 202 {runId}

    Q->>AI: process + analyze
    AI->>ST: fetch document bytes
    AI->>AI: OCR + parse + chunk
    AI->>QD: upsert embeddings(chunks)
    AI->>AI: run LangGraph agents (facts→issues→statutes→args→...)
    AI->>API: POST /internal/analysis/{runId}/results (signed)
    API->>DB: persist facts/issues/statutes/args/irac + run=COMPLETED
    FE->>API: GET /analysis/{runId} (poll/SSE)
    API-->>FE: status + ready sections
    FE->>FE: render Case Analysis Dashboard
```

### 4.3 RAG chat (grounded)

```mermaid
sequenceDiagram
    actor U as User
    participant FE as SPA
    participant API as ChatController
    participant AI as AI Service
    participant QD as Qdrant
    participant LLM as LLM Provider
    participant DB as PostgreSQL

    U->>FE: ask question about the case
    FE->>API: POST /cases/{id}/chat {question, sessionId}
    API->>API: RBAC: can read case?
    API->>AI: /chat {caseId, question, history}
    AI->>LLM: embed(question)
    AI->>QD: search(top-k, filter caseId)
    QD-->>AI: relevant chunks + source refs
    AI->>LLM: answer with context + citation instruction
    LLM-->>AI: answer
    AI->>AI: attach citations, flag low-confidence
    AI-->>API: {answer, citations[]}
    API->>DB: persist chat_message(user + assistant)
    API-->>FE: {answer, citations[]}
    FE->>FE: render answer + clickable source citations
```

---

## 5. API Surface (v1 — preview; full OpenAPI in Phase 4)

| Method | Path | Auth | Permission |
|---|---|---|---|
| POST | `/api/v1/auth/register` | public | — |
| POST | `/api/v1/auth/login` | public | — |
| POST | `/api/v1/auth/refresh` | public(token) | — |
| POST | `/api/v1/auth/forgot-password` | public | — |
| POST | `/api/v1/auth/reset-password` | public(token) | — |
| GET | `/api/v1/cases` | JWT | `case:read` |
| POST | `/api/v1/cases` | JWT | `case:create` |
| GET | `/api/v1/cases/{id}` | JWT | `case:read` |
| POST | `/api/v1/cases/{id}/documents` | JWT | `document:upload` |
| GET | `/api/v1/documents/{id}/status` | JWT | `case:read` |
| POST | `/api/v1/cases/{id}/analyze` | JWT | `analysis:run` |
| GET | `/api/v1/analysis/{runId}` | JWT | `case:read` |
| GET | `/api/v1/cases/{id}/dashboard/overview` | JWT | `case:read` |
| GET | `/api/v1/cases/{id}/dashboard/{section}` | JWT | `case:read` |
| GET | `/api/v1/cases/{id}/irac` | JWT | `irac:view` |
| GET | `/api/v1/cases/{id}/brief` | JWT | `case:read` |
| POST | `/api/v1/cases/{id}/chat` | JWT | `case:read` |
| GET | `/api/v1/admin/users` | JWT | `user:manage` |
| GET | `/api/v1/admin/audit` | JWT | `audit:read` |
| POST | `/internal/analysis/{runId}/results` | service-token | internal |

Standard envelope:
```json
{ "data": { }, "error": null, "traceId": "..." }
```
Error:
```json
{ "data": null, "error": { "code": "CASE_NOT_FOUND", "message": "…", "details": [] }, "traceId": "…" }
```

---

## 6. Concurrency & Async Model

- API requests handled on Java 21 **virtual threads** (high I/O concurrency, simple code).
- Heavy work dispatched to a **job queue** (MVP: DB-backed jobs + Spring `@Async` executor;
  prod: Redis/RabbitMQ — see [ADR-0007](06-adrs.md)). Jobs are **idempotent** (keyed by
  `runId`) and retried with exponential backoff.
- AI tier runs CPU/IO-bound steps (OCR, embeddings) in worker processes; LLM calls are
  awaited with timeouts + circuit breaker.

---

## 7. Error & Resilience Strategy

| Failure | Behavior |
|---|---|
| AI tier down/timeout | Run marked `FAILED` with reason; dashboards already-computed remain viewable; user can retry. |
| OCR/parse failure on a doc | Document marked `FAILED`; surfaced in Document Monitoring; other docs proceed. |
| LLM rate limit | Backoff + retry; degrade to cheaper model if configured. |
| Partial agent failure | Persist successful agent outputs; mark failed agents; allow re-run of subset. |
| DB constraint violation | Mapped to `409`/`422` with clear `code`; never leak stack traces. |

---

_Previous: [← Architecture Overview](01-architecture-overview.md) · Next: [Database Design →](03-database-design.md)_
