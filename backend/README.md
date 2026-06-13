# LexMind AI — Backend (Spring Boot 3 / Java 21)

The application/API tier: authentication & RBAC, case workspace, document intake, and AI
analysis orchestration. System of record is PostgreSQL; the AI tier (FastAPI) is called over
REST. See [docs/phase-02-architecture](../docs/phase-02-architecture) for the design.

## Stack
Java 21 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA · Flyway · PostgreSQL ·
springdoc-openapi · Lombok · Testcontainers (tests).

## Prerequisites
- JDK 21
- A PostgreSQL 16 database (or use the Compose stack from Phase 9)
- Maven (or the bundled wrapper once generated)

## Configure
Copy `.env.example` → `.env` (or export the vars). Key settings live in
[`application.yml`](src/main/resources/application.yml) under `lexmind.*`.

```bash
createdb lexmind            # or: docker run -e POSTGRES_DB=lexmind -e POSTGRES_USER=lexmind \
                            #     -e POSTGRES_PASSWORD=lexmind -p 5432:5432 postgres:16
export DB_URL=jdbc:postgresql://localhost:5432/lexmind
export DB_USERNAME=lexmind DB_PASSWORD=lexmind
export JWT_SECRET="a-strong-256-bit-secret-..."
```

## Run
```bash
mvn spring-boot:run
# API:     http://localhost:8080/api/v1
# Swagger: http://localhost:8080/swagger-ui.html
# Health:  http://localhost:8080/actuator/health
```
Flyway applies [`db/migration/V1__init.sql`](src/main/resources/db/migration/V1__init.sql)
on startup (schema + seeded roles/permissions).

## Module map (`ai.lexmind`)
```
common/      security (JWT/RBAC), error envelope, audit (AOP), config (OpenAPI/CORS), web
auth/        register/login/refresh/reset, User/Role/Permission, JWT issuance
organization/ firm tenant
casefile/    cases (matters), parties, members, CaseAccessService (central RBAC guard)
document/    upload/list/download, StorageService (local FS | S3)
analysis/    AnalysisRun/AgentExecution, AiServiceClient, async orchestrator, ingest, callbacks
intelligence/ read-side: facts, timeline, issues, arguments, IRAC + Dashboard API
```

## Key endpoints (full contract in Swagger)
```
POST /api/v1/auth/register | login | refresh | forgot-password | reset-password   (public)
GET  /api/v1/auth/me
GET  /api/v1/cases            POST /api/v1/cases            GET /api/v1/cases/{id}
POST /api/v1/cases/{id}/documents     GET /api/v1/cases/{id}/documents
GET  /api/v1/documents/{id}/status | download
POST /api/v1/cases/{id}/analyze       GET /api/v1/analysis/{runId}
GET  /api/v1/cases/{id}/dashboard/{overview|facts|timeline|issues|arguments}
GET  /api/v1/cases/{id}/irac
POST /internal/analysis/{runId}/results        (service-token only)
```

## Security model
Stateless JWT (access ~15m + rotating refresh). Permissions (`case:read`, `analysis:run`, …)
are embedded in the token and enforced with `@PreAuthorize`. Per-case access (owner/firm
scope) is centralized in `CaseAccessService`. Audit entries are written via the `@Audited`
aspect + `AuditService`.

## Notes / scope
- The AI service (FastAPI) is built in Phase 6; until then, `POST /analyze` marks runs
  `FAILED` if the AI tier is unreachable — the rest of the API is fully functional.
- Dockerfile + Compose + CI are delivered in Phase 9.
- Evidence/witness/precedent/analytics persistence + chat are added in Phases 6–7 following
  the same Controller→Service→Repository pattern shown here.
