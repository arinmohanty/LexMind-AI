# Viva Questions & Answers

## LexMind AI

**Date:** 2026-06-14 · A preparation aid for the project viva.

---

## A. Project Overview

**Q1. What is LexMind AI in one sentence?**
A web platform that ingests a user's legal documents and turns them into a structured, grounded
legal-intelligence dashboard (facts, issues, statutes, arguments, IRAC, analytics) using LLMs,
RAG, and a 7-agent pipeline — framed as *analysis, not legal advice*.

**Q2. What problem does it solve?**
Legal professionals/students spend most analysis time on document mechanics (reading,
transcribing, organizing) instead of reasoning. LexMind automates the mechanical extraction into
structured, reusable intelligence.

**Q3. Who are the users?**
Law students, advocates, legal researchers, law-firm admins, and a platform super-admin — five
RBAC roles.

**Q4. Why is it not just a chatbot or summarizer?**
It produces *structured* outputs (fact matrix, ranked issues, side-by-side arguments, IRAC,
analytics) over the user's *own* documents, with citations, RBAC, and audit — not flat prose from
a general model.

**Q5. What does "analysis, not legal advice" mean and why does it matter?**
Outputs are decision-support aids requiring professional judgment; this keeps the system within
ethical/legal bounds (no liability for advice/outcomes) and is reinforced in the UI and prompts.

---

## B. Architecture

**Q6. Describe the overall architecture.**
Three independently deployable tiers: a React SPA, a Spring Boot REST API (system of record:
PostgreSQL), and a FastAPI AI service (LangGraph agents + RAG; vector store: Qdrant). They
communicate over REST; the backend↔AI link uses an internal service token.

**Q7. Why three tiers instead of a monolith?**
Heavy AI work (OCR, embeddings, multi-agent LLM calls) must not block the transactional API; the
AI ecosystem is Python-native; separate tiers scale and fail independently and allow graceful
degradation (dashboards stay viewable if AI is down). (ADR-0002.)

**Q8. How do the backend and AI service communicate?**
The backend calls the AI service's `/analyze` and `/chat` over REST with an `X-Internal-Token`
header. The AI service fetches document bytes back from the backend's `/internal/documents/{id}/content`.

**Q9. What makes the API horizontally scalable?**
It's stateless (JWT auth, no server session); any replica can serve any request, and heavy work is
offloaded to async workers / the AI tier.

**Q10. How does asynchronous analysis work?**
`POST /analyze` creates an `analysis_run` and returns `202` immediately; an async orchestrator
calls the AI tier off-thread and persists results; the SPA polls `/analysis/{runId}` and fills the
dashboard progressively. The job is committed before the async worker starts (no read-before-commit
race).

---

## C. AI / ML

**Q11. What are the 7 agents and their order?**
Fact Extraction → Issue Identification → {Statute Analysis, Argument Builder} → {Precedent
Research, Risk Analysis} → Judge Perspective → IRAC composer. Facts/timeline, issues, arguments,
and IRAC become structured lists; statute/precedent/risk/judge outputs are stored as agent
telemetry.

**Q12. What is RAG and why use it?**
Retrieval-Augmented Generation grounds the model's answer in retrieved document chunks, drastically
reducing hallucination and enabling **citations**. We retrieve case-scoped chunks from Qdrant,
prompt the LLM to answer only from them, and attach source citations.

**Q13. How do you prevent hallucinated citations?**
Prompts forbid outside knowledge for case facts and require "not in documents" when unsupported;
retrieval is case-scoped; answers carry citations; ungrounded answers are forced to **Low**
confidence; a verifier step checks claims against chunks.

**Q14. What is LangGraph and why use it?**
A framework for building stateful multi-agent graphs with conditional edges, retries, and
partial-failure handling — a natural fit for the 7-agent pipeline. We also provide a sequential
fallback if LangGraph isn't installed.

**Q15. How do you handle an agent failing mid-run?**
Each agent is wrapped with telemetry + try/except; a failure records a `FAILED` execution and the
run continues, ending as **PARTIAL** rather than failing entirely. (Tested in `test_runner_partial`.)

**Q16. What is the "mock provider" and why does it exist?**
A deterministic LLM/embedding implementation that returns canned, contract-shaped output with no
API key or network — it lets the whole graph, RAG, and tests run offline and enables zero-cost
demos. Production flips `LLM_PROVIDER=anthropic`.

**Q17. How is the LLM abstracted?**
Behind an `LLMProvider` interface (`complete(system, user)`), selected by config. This avoids
lock-in and allows swapping to a self-hosted model for sensitive deployments. (ADR-0008.)

**Q18. How is document text chunked and embedded?**
Extracted text (OCR/PDF/DOCX) is split into ~800-token windows with ~100-token overlap, embedded,
and upserted to Qdrant with payload `{caseId, documentId, page}`; PostgreSQL stores chunk text +
the Qdrant point id.

**Q19. Which vector database and why Qdrant over ChromaDB?**
Qdrant — production-grade ANN with first-class payload filtering (essential for per-case
isolation), Docker/managed options, and horizontal scale. Chroma is lighter, better for
prototyping. (ADR-0001.)

**Q20. How do you ensure one case can't see another's vectors?**
Every search filters by `caseId` payload; the in-memory fallback filters by case too. This is both
a correctness and a security boundary (tested in `test_vectorstore`).

---

## D. Backend / Security

**Q21. How does authentication work?**
Stateless JWT: a short-lived access token (~15 min) carries the user id, role, and permissions; a
rotating refresh token (stored hashed) issues new access tokens. (ADR-0006.)

**Q22. How is RBAC enforced?**
Permissions (e.g. `case:read`) are embedded in the JWT and checked with `@PreAuthorize`. Per-case
access (owner/firm scope) is centralized in `CaseAccessService`, reused by documents, analysis, and
dashboards.

**Q23. Why return 404 (not 403) when a user accesses someone else's case?**
To hide existence — a 403 would reveal that the resource exists. (Tested in `CaseRbacIT`.)

**Q24. How are passwords and tokens stored?**
Passwords with BCrypt; refresh and password-reset tokens are stored as SHA-256 hashes (the raw
token is never persisted).

**Q25. How do you avoid user enumeration?**
Login returns a uniform `INVALID_CREDENTIALS` 401 regardless of cause; forgot-password always
returns success whether or not the email exists.

**Q26. Which OWASP Top-10 risks did you address?**
Broken access control (RBAC + per-case guard + tests), auth failures (JWT verify, non-enumerating
messages), injection (parameterized JPA, DTO validation), sensitive-data exposure (no stack traces,
hashed tokens), SSRF/secrets (internal token on `/internal`).

**Q27. What is the standard API response format?**
A JSON envelope `{ data, error, traceId }`; errors carry a stable `code`, a safe `message`, and
optional field details. A global exception handler maps exceptions to this without leaking stack
traces.

**Q28. Why Flyway and `ddl-auto=validate`?**
Flyway owns the schema via versioned migrations (`V1__init.sql`); Hibernate only *validates* its
mappings against it — schema changes are explicit, reviewed, and reproducible.

**Q29. How does the database model balance normalization and flexibility?**
3NF for transactional/reference data; JSONB only for genuinely variable payloads (briefs,
citations, findings, audit metadata). UUID PKs, soft deletes, an append-only audit log.

**Q30. What does the audit subsystem record and how?**
An AOP aspect + explicit `AuditService` writes append-only `audit_logs` (actor, action,
resource, ip/ua, metadata) asynchronously so auditing never slows the request path.

---

## E. Frontend

**Q31. Why React Query?**
It manages server state (caching, polling, invalidation) cleanly — ideal for the async-analysis
"poll until complete, then refresh the dashboard tabs" pattern.

**Q32. How does the dashboard "fill progressively"?**
After analyze, the SPA polls the run; on completion it invalidates the per-section dashboard
queries so tabs refetch and populate — matching the agents finishing.

**Q33. How do design and code stay in sync?**
Design tokens are CSS variables that both Tailwind and the ShadCN components read — one source of
truth for colors/typography across light/dark.

**Q34. How is the frontend secured against showing unauthorized UI?**
A client-side permission map mirrors the backend RBAC to show/hide nav and screens — but the
backend remains the real authority (UI hiding is UX, not security).

---

## F. Testing & Deployment

**Q35. What is your testing strategy?**
A wide unit base + focused integration tests on risky seams (auth, RBAC isolation, the AI contract,
RAG grounding). Tools: JUnit + Testcontainers (backend), pytest (AI), Vitest (frontend).

**Q36. What coverage did you achieve?**
AI service 82% (measured); frontend core libs/components covered with a clean build; backend
measured via JaCoCo on `mvn verify` in CI/Docker.

**Q37. How do integration tests run a real database?**
Testcontainers spins an ephemeral PostgreSQL, Flyway applies migrations, and MockMvc drives the API
— so tests run against a real schema, not mocks.

**Q38. How is the system deployed?**
Per-service Dockerfiles + a `docker-compose` stack (Postgres, Qdrant, backend, AI, frontend). One
command brings it up; CI builds and tests all tiers; cloud targets are Railway/Render/AWS.

**Q39. Where does the backend actually get compiled?**
In its Docker build stage (`maven:3.9-eclipse-temurin-21`) and in the CI `backend` job
(`mvn verify` on JDK 21) — both compile and run its tests.

---

## G. Conceptual / Design Decisions

**Q40. What was the hardest design trade-off?**
Trust vs. fluency in AI output. We chose grounding (RAG + citations + confidence + disclaimers)
over free-form fluency, accepting extra pipeline steps/tokens because hallucinated legal citations
are unacceptable.

**Q41. How would you scale to thousands of concurrent analyses?**
A real queue (Redis/RabbitMQ) with autoscaling AI workers, model routing + caching for cost,
PostgreSQL read replicas, and independent Qdrant scaling — the architecture already separates these
concerns. (ADR-0007.)

**Q42. What are the system's limitations?**
Evidence/witness/precedent persistence and the research portal are scoped to later releases;
readiness scoring is currently heuristic; English India-first corpus; backend tests run in CI/Docker
rather than this dev box.

**Q43. How is multitenancy handled?**
Shared schema with `organization_id` row-scoping enforced centrally, with optional Postgres RLS
later — simple and cost-effective for v1. (ADR-0009.)

**Q44. What would you add next?**
Evidence/witness/precedent dashboards + similar-case discovery, cross-exam/strategy assistants,
S3 storage, SSO/2FA, self-hosted models for sensitive tenants, and native mobile apps reusing the
same API.

**Q45. What did you learn building this?**
End-to-end product engineering: contract-first multi-service design, applied AI with grounding,
security/RBAC, testing strategy, and containerized delivery — plus the discipline of building in
reviewed phases.
