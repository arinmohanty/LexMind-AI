# LexMind AI — Architecture Decision Records (ADRs)

**Document:** Phase 2 / 06
**Status:** Accepted (v0.1)
**Owner:** Architecture
**Last updated:** 2026-06-14

> Each ADR captures one significant decision: context, the decision, alternatives, and
> consequences. ADRs are immutable once accepted; changes are made by superseding ADRs.

---

## ADR-0001 — Qdrant as the vector database
**Status:** Accepted
**Context:** We need ANN similarity search for RAG and similar-case discovery, with
per-tenant/per-case filtering and a path to production scale.
**Decision:** Use **Qdrant**.
**Alternatives:** ChromaDB (simpler, great for prototyping but lighter for prod scale/
filtering); pgvector (one less service, but weaker ANN ergonomics at scale).
**Consequences:** (+) production-grade filtered ANN, Docker + managed options, horizontal
scale; (−) one more service to run vs pgvector. Mitigation: Compose service in dev, managed/
container in prod.

---

## ADR-0002 — Three-tier split: SPA / Spring API / FastAPI AI service
**Status:** Accepted
**Context:** Heavy, slow AI workloads (OCR, embeddings, multi-agent LLM calls) must not
degrade the transactional API; the AI ecosystem is Python-native.
**Decision:** Separate **React SPA**, **Spring Boot API** (system of record + RBAC), and a
**FastAPI AI service** (agents, RAG, pipeline).
**Alternatives:** Monolith (AI inside Spring via Java LLM libs) — weaker AI ecosystem, couples
scaling; all-Python backend — loses Spring's mature security/JPA.
**Consequences:** (+) independent scaling, fault isolation, best tool per concern, graceful
degradation; (−) cross-service contract + auth complexity. Mitigation: OpenAPI contract +
service-token auth + correlation ids.

---

## ADR-0003 — Java 21 + Spring Boot 3 for the application tier
**Status:** Accepted
**Context:** Transactional core needs strong typing, mature security, ORM, and concurrency.
**Decision:** **Java 21 + Spring Boot 3** (Spring Security, Spring Data JPA), virtual threads
for high-I/O concurrency.
**Alternatives:** Node/Nest, Go. **Consequences:** (+) mature security/RBAC, JPA, ecosystem,
required by project stack; (−) heavier than Node for trivial endpoints — acceptable.

---

## ADR-0004 — FastAPI + LangGraph for AI orchestration
**Status:** Accepted
**Context:** We have a 7-agent pipeline with shared state, conditional edges, retries, and
partial-failure handling.
**Decision:** **FastAPI** for the service boundary; **LangGraph** for stateful multi-agent
orchestration.
**Alternatives:** Plain function chaining (no built-in state/edges/retries); other agent
frameworks. **Consequences:** (+) explicit graph, resumability, partial results; (−) added
dependency + learning curve.

---

## ADR-0005 — PostgreSQL as the single system of record (normalized + JSONB)
**Status:** Accepted
**Context:** Legal data is relational and integrity-critical; some AI payloads are variable.
**Decision:** **PostgreSQL**, normalized to 3NF for transactional/reference data, **JSONB**
only for genuinely variable payloads (briefs, citations, findings, audit metadata).
**Alternatives:** Document DB (weak relational integrity); pure-JSON in PG (loses
queryability). **Consequences:** (+) integrity + flexible payloads; (−) discipline needed to
not overuse JSONB.

---

## ADR-0006 — Stateless JWT authentication
**Status:** Accepted
**Context:** Horizontal scale + future mobile reuse of the same API.
**Decision:** **JWT access tokens (~15 min)** + **persisted refresh tokens** (rotation +
revocation). RBAC via roles→permissions evaluated at the API.
**Alternatives:** Server sessions (stateful, scaling friction). **Consequences:** (+)
stateless scale, mobile-ready; (−) token revocation needs refresh-token store — included.

---

## ADR-0007 — Asynchronous job pipeline for document & AI processing
**Status:** Accepted
**Context:** OCR + multi-agent analysis take seconds–minutes; requests must stay responsive.
**Decision:** **Async jobs** with status tracking. MVP: DB-backed jobs + Spring `@Async`
executor; **prod: Redis/RabbitMQ** queue with autoscaling workers. Jobs idempotent by `runId`,
retried with backoff.
**Alternatives:** Synchronous processing (timeouts, poor UX). **Consequences:** (+)
responsive API, scalable workers, resilient; (−) eventual-consistency UX (polling/SSE) —
acceptable and expected for analysis.

---

## ADR-0008 — Provider-abstracted LLM/embeddings, default latest Claude
**Status:** Accepted
**Context:** Avoid lock-in; allow self-hosting for sensitive deployments; control cost via
model routing.
**Decision:** All model calls go through an **LLMProvider/EmbeddingProvider interface**.
Default to latest **Claude** (e.g. `claude-opus-4-8` for heavy reasoning, a smaller/faster
Claude for cheap steps). Swap to self-hosted/open models via config.
**Alternatives:** Hard-coded single provider. **Consequences:** (+) flexibility, cost
routing, privacy option; (−) lowest-common-denominator interface; provider-specific features
behind capability flags.

---

## ADR-0009 — Multitenancy via shared schema + row scoping (v1)
**Status:** Accepted
**Context:** Firms need isolation; capstone resource constraints favor simplicity.
**Decision:** **Shared schema** with `organization_id` scoping enforced in the app (optional
Postgres RLS later). Solo users have `organization_id = NULL`.
**Alternatives:** Schema-per-tenant / DB-per-tenant (stronger isolation, heavier ops).
**Consequences:** (+) simple, cost-effective, good enough for v1; (−) isolation depends on
app correctness → mitigated by centralized access checks + tests + optional RLS.

---

## ADR-0010 — Grounded AI with mandatory citations & "no legal advice"
**Status:** Accepted
**Context:** High-stakes domain; hallucinated citations are unacceptable; legal/ethical
liability.
**Decision:** RAG answers and agent outputs must be **grounded in retrieved chunks with
citations**; a **verifier** flags unsupported claims; outputs are framed as analysis, never
advice, with persistent disclaimers and human-in-the-loop.
**Alternatives:** Ungrounded generation (unacceptable risk). **Consequences:** (+) trust,
defensibility; (−) more pipeline steps + tokens — justified by the domain.

---

## ADR-0011 — UUID primary keys + soft deletes for core entities
**Status:** Accepted
**Context:** IDs are exposed in URLs/APIs; we must preserve audit/billing history.
**Decision:** **UUID** PKs (`gen_random_uuid()`); **soft-delete** via status for `users`/
`cases`; hard cascade only for owned children.
**Alternatives:** Auto-increment ints (enumeration risk), hard deletes (lose history).
**Consequences:** (+) safe to expose, merge-friendly, auditable; (−) slightly larger keys/
indexes — negligible.

---

## ADR-0012 — Object storage abstraction (local FS dev, S3-compatible prod)
**Status:** Accepted
**Context:** Original documents are sensitive and large; dev should be frictionless.
**Decision:** `StorageService` interface with **local filesystem** (dev) and **S3-compatible**
(MinIO/S3) prod implementations; encryption at rest in prod; signed/short-lived access.
**Alternatives:** Store blobs in PG (bloats DB), single hard-coded backend. **Consequences:**
(+) frictionless dev, scalable secure prod; (−) two implementations to maintain — small.

---

## Decision Index

| ADR | Decision | Status |
|---|---|---|
| 0001 | Qdrant vector DB | Accepted |
| 0002 | 3-tier SPA/Spring/FastAPI | Accepted |
| 0003 | Java 21 + Spring Boot 3 | Accepted |
| 0004 | FastAPI + LangGraph | Accepted |
| 0005 | PostgreSQL SoR (3NF + JSONB) | Accepted |
| 0006 | Stateless JWT auth | Accepted |
| 0007 | Async job pipeline | Accepted |
| 0008 | Provider-abstracted Claude | Accepted |
| 0009 | Shared-schema multitenancy | Accepted |
| 0010 | Grounded AI + no legal advice | Accepted |
| 0011 | UUID PKs + soft deletes | Accepted |
| 0012 | Object storage abstraction | Accepted |

---

_Previous: [← AI Architecture](05-ai-architecture.md) · Phase 2 complete._
