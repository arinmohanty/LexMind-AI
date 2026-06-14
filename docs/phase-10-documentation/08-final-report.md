# Final MCA Project Report

# LexMind AI
## Legal Intelligence, Case Analysis & Litigation Strategy Platform

**Submitted by:** _[Student Name]_ · **Roll No.:** _[___]_
**Programme:** Master of Computer Applications (MCA)
**Project Guide:** _[Guide Name]_
**Institution:** _[College / University]_ · **Academic Year:** 2025–2026

---

## Certificate _(template)_
This is to certify that the project entitled **"LexMind AI — Legal Intelligence, Case Analysis &
Litigation Strategy Platform"** is a bona fide record of work carried out by _[Student Name]_ in
partial fulfilment of the requirements for the award of the degree of Master of Computer
Applications, under my guidance.

_Guide: ____________  ·  HOD: ____________  ·  External Examiner: _____________

## Declaration _(template)_
I declare that this project is my own work and has not been submitted elsewhere. External
libraries/services are used under their respective licenses and acknowledged.

## Acknowledgements _(template)_
I thank my guide, faculty, and family for their support.

---

## Abstract

Legal work is document-dense and cognitively heavy; extracting structured meaning from FIRs,
charge sheets, petitions, judgments, contracts, and evidence is done manually, making it slow,
inconsistent, and non-reusable. **LexMind AI** is a production-grade web platform that ingests a
user's legal documents and transforms them into a **structured Legal Intelligence Dashboard**
using Large Language Models, Retrieval-Augmented Generation, and a seven-agent analysis pipeline.
It produces facts, timelines, issues, applicable statutes, side-by-side arguments, IRAC analyses,
case briefs, grounded chat with citations, and analytics (case strength, risk, litigation
readiness) — all framed as *analysis, not legal advice*, with role-based access control and audit
logging. The system is built as three independently deployable tiers (React SPA, Spring Boot API,
FastAPI AI service) over PostgreSQL and Qdrant, containerized with Docker, and developed across
ten reviewed phases. Automated tests cover the critical paths; the AI tier achieves 82% coverage.

---

## Table of Contents
1. Introduction 2. Literature Survey 3. System Analysis 4. System Design 5. Implementation
6. Testing 7. Deployment 8. Results & Discussion 9. Conclusion & Future Scope · Bibliography ·
Appendices.

---

## Chapter 1 — Introduction

### 1.1 Background
The legal sector is among the least digitized professional services. AI (LLMs + RAG) and court
digitization make AI-assisted case analysis newly viable. (Detail:
[Synopsis](01-synopsis.md), [Problem Statement](../phase-01-product/01-problem-statement.md).)

### 1.2 Problem Statement
Legal professionals and students spend most analysis time on document mechanics, not reasoning.
No affordable, trustworthy tool turns a user's *own* documents into *structured, grounded* legal
intelligence with access control and auditability.

### 1.3 Objectives
Ingest documents (OCR); produce a structured dashboard via 7 AI agents; generate IRAC + briefs;
provide grounded RAG chat; compute analytics; enforce RBAC + audit; ship a responsive,
containerized SaaS. (Full list: [Synopsis §4](01-synopsis.md).)

### 1.4 Scope
v1 covers ingestion, dashboards, IRAC, brief, chat, analytics, RBAC, audit, exports, and the
public site. Out of scope: legal advice, drafting/CLM, billing, e-filing, non-English corpora.

### 1.5 Organization of the report
Chapters follow the SDLC: analysis → design → implementation → testing → deployment → results.

---

## Chapter 2 — Literature Survey
Existing tools either *find* law (SCC Online, Manupatra, CaseMine, Lexis+, Westlaw) or *generate*
prose (generic chatbots), but none reliably turn a user's own documents into structured, grounded
intelligence across education and practice. LexMind AI fills this gap with LLMs + RAG +
multi-agent orchestration. (Full survey: [Literature Survey](03-literature-survey.md).)

---

## Chapter 3 — System Analysis

### 3.1 Existing system & limitations
Manual analysis; research databases without per-matter analysis; ungrounded chatbots.

### 3.2 Proposed system
A three-tier AI platform producing structured, grounded, role-scoped legal intelligence.

### 3.3 Feasibility
- **Technical:** mature stacks (Spring Boot, FastAPI, React) + proven AI patterns (RAG, LangGraph).
- **Economic:** open-source/managed services; mock provider enables zero-cost demos; tiered LLM costs.
- **Operational:** human-in-the-loop; "analysis, not legal advice" keeps scope safe.

### 3.4 Requirements
Functional + non-functional requirements are specified in the [SRS](02-srs.md). Five user roles
with a full permission matrix; 17 functional requirements; 9 NFR categories.

---

## Chapter 4 — System Design
Three-tier, service-oriented architecture; Controller→Service→Repository layering; normalized
PostgreSQL (3NF + JSONB) with Qdrant for vectors; a LangGraph 7-agent pipeline; stateless JWT
security with central per-case access control; design system + wireframes + component hierarchy.
(Full design: [SDD](04-sdd.md); diagrams in [Phase 2](../phase-02-architecture/) and
[Phase 3](../phase-03-uiux/).)

**Key diagrams:** C4 context/container/component, deployment topology, class + sequence diagrams
(Phase 2); ER diagram + DDL; AI graph; sitemap + 15 wireframes + React component tree (Phase 3).

---

## Chapter 5 — Implementation

### 5.1 Technology stack
React/TS/Vite/Tailwind/ShadCN/React Query/Recharts (frontend); Java 21/Spring Boot 3/Security/JPA/
Flyway (backend); Python/FastAPI/LangGraph (AI); PostgreSQL + Qdrant; Docker.

### 5.2 Modules implemented
- **Backend (≈70 Java files):** auth + RBAC (JWT, rotating refresh), cases + parties, documents +
  storage abstraction, async analysis orchestration + AI client + result ingestion, the Case
  Dashboard read API, analytics API, audit aspect, OpenAPI.
- **Frontend (≈58 files):** API client with silent JWT refresh, role-aware app shell, auth pages,
  case repository, upload→analyze wizard, the tabbed Case Analysis Dashboard (incl. Analytics with
  Recharts), IRAC, theming.
- **AI service (≈38 Python files):** document pipeline (OCR/parse/chunk/embed), the 7-agent
  LangGraph (+ sequential fallback), RAG chat, provider abstraction (mock + Claude), Qdrant +
  in-memory vector stores.

### 5.3 Notable implementation decisions
Grounded AI with mandatory citations; provider-abstracted LLM (swap mock↔Claude↔self-hosted);
async pipeline with partial-failure tolerance; idempotent re-analysis; shared-schema multitenancy
with central access guard. (Rationale: [ADRs](../phase-02-architecture/06-adrs.md).)

### 5.4 Code organization
A git monorepo: `frontend/ backend/ ai-service/ infra/ docs/` developed over ten phases with one
commit per phase (`git log` is the development record).

---

## Chapter 6 — Testing
Unit, integration/API, and security tests across all tiers (JUnit + Testcontainers; pytest;
Vitest). AI tier: 20 tests, **82% coverage**; frontend: 18 tests + clean build; backend suite runs
via `mvn verify` (Testcontainers + JaCoCo). Three real defects were found and fixed during
development. (Full report: [Testing Report](06-testing-report.md).)

---

## Chapter 7 — Deployment
Containerized with per-service Dockerfiles (the backend image compiles on JDK 21) and a
`docker-compose` stack (Postgres, Qdrant, backend, AI, frontend). One command brings the system up;
cloud targets are Railway/Render/AWS; CI runs all suites on GitHub Actions. (Full guide:
[Deployment Guide](../phase-09-deployment/deployment.md).)

---

## Chapter 8 — Results & Discussion
The platform delivers the intended core loop: **upload → AI analysis → structured dashboard +
IRAC + analytics + grounded chat → export**, runnable end-to-end (with a deterministic mock AI for
zero-cost demos, or real Claude in production). The frontend build and AI test suite are verified;
the backend compiles and tests in CI/Docker. Trust is engineered in (citations, confidence,
disclaimers, RBAC, audit). Limitations: evidence/witness/precedent persistence and the research
portal are scoped to later releases; analytics readiness is currently heuristic pending the
witness/evidence agents.

---

## Chapter 9 — Conclusion & Future Scope

### 9.1 Conclusion
LexMind AI demonstrates that a user's legal documents can be turned into **structured, grounded,
role-scoped legal intelligence** within minutes, combining production engineering with applied AI,
and respecting the ethical boundary of "analysis, not legal advice."

### 9.2 Future Scope
- Evidence/witness/precedent dashboards + similar-case discovery over a seeded judgment corpus.
- Cross-examination & litigation-strategy assistants; hearing-prep exports.
- Research portal: citation networks, jurisdiction trends.
- Firm collaboration, SSO/2FA, notifications; S3 storage; self-hosted models for sensitive tenants.
- Native mobile apps reusing the same REST API.
- Model-routing + caching for cost; an eval harness over golden cases.

---

## Bibliography
See [Literature Survey §6](03-literature-survey.md#6-indicative-references). Framework
documentation: Spring Boot, FastAPI, React, LangGraph, Qdrant, PostgreSQL. _[verify all at final
submission]_.

## Appendices
- **A. ER diagram & schema:** [Database Design](../phase-02-architecture/03-database-design.md),
  [`04-schema.sql`](../phase-02-architecture/04-schema.sql).
- **B. API contract:** Swagger at `/swagger-ui.html`; surface in
  [LLD §5](../phase-02-architecture/02-low-level-design.md).
- **C. Wireframes:** [Wireframes](../phase-03-uiux/03-wireframes.md).
- **D. Viva Q&A:** [09-viva-qa.md](09-viva-qa.md).

---

## Traceability
| FR | Feature | Backend endpoint | UI | Test |
|---|---|---|---|---|
| FR-1/3 | Auth + RBAC | `/api/v1/auth/*`, `@PreAuthorize` | Login/Register | `AuthFlowIT`, `JwtServiceTest`, rbac.test |
| FR-5/6 | Upload + OCR | `/cases/{id}/documents`, AI `/process` | Upload wizard | `test_pipeline`, `test_api` |
| FR-7/8 | Analysis + dashboard | `/cases/{id}/analyze`, `/dashboard/*` | Case workspace | `test_graph_mock`, `CaseRbacIT` |
| FR-9 | IRAC | `/cases/{id}/irac` | IRAC tab/page | `test_graph_mock` |
| FR-11 | RAG chat | AI `/chat` | Chat | `test_rag` |
| FR-12 | Analytics | `/cases/{id}/analytics`, `/analytics/portfolio` | Analytics tab/center | `test_graph_mock` (analytics) |
| FR-14/16 | Audit + isolation | audit aspect, `CaseAccessService` | — | `CaseAccessServiceTest`, `CaseRbacIT` |
