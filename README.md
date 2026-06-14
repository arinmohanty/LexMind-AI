# LexMind AI

**Legal Intelligence, Case Analysis & Litigation Strategy Platform**

LexMind AI is a production-grade legal technology platform that ingests legal documents
(FIRs, charge sheets, judgments, petitions, affidavits, contracts, written statements,
evidence and witness records) and transforms them into **structured legal intelligence
dashboards** for law students, advocates, legal researchers, and law firms.

It is not a legal chatbot, a document summarizer, or a CRUD app. It is a **Legal
Operating System for case analysis** — organizing legal thinking, accelerating research,
and assisting litigation strategy and legal education.

> ⚖️ **Disclaimer:** LexMind AI assists with legal analysis and education. It does **not**
> provide legal advice and is **not** a substitute for a qualified advocate.

---

## Monorepo Structure

```
lexmind-ai/
├── frontend/      # React 18 + TypeScript + Vite + TailwindCSS + ShadCN UI
├── backend/       # Java 21 + Spring Boot 3 (REST, Security/JWT, JPA)
├── ai-service/    # Python + FastAPI + LangGraph (agents, RAG, OCR)
├── infra/         # Docker, docker-compose, env, CI/CD
└── docs/          # PRD, SRS, architecture, ER diagrams, MCA report, etc.
    └── phase-01-product/
```

## Run it (full stack)

```bash
cd infra && cp .env.example .env && docker compose up --build
# App: http://localhost:8081 · API: http://localhost:8080/swagger-ui.html · AI: http://localhost:8000/docs
```
Runs with `LLM_PROVIDER=mock` (no API key). See [docs/phase-09-deployment](docs/phase-09-deployment/deployment.md).

## Technology Stack

| Layer        | Technology                                                              |
|--------------|-------------------------------------------------------------------------|
| Frontend     | React, TypeScript, Vite, TailwindCSS, ShadCN UI, React Query, Recharts  |
| Backend      | Java 21, Spring Boot 3, Spring Security, JWT, Spring Data JPA, REST      |
| AI Service   | Python, FastAPI, LangGraph, RAG, LLM integration                        |
| Database     | PostgreSQL                                                              |
| Vector DB    | Qdrant                                                                   |
| Doc Pipeline | OCR (Tesseract), PDF/DOCX parsing, metadata extraction                  |
| DevOps       | Docker, Docker Compose, GitHub Actions                                  |
| Docs/API     | Swagger / OpenAPI                                                       |

## Build Roadmap (10 Phases)

| Phase | Deliverable                                            | Status        |
|-------|--------------------------------------------------------|---------------|
| 1     | Product foundation (PRD, market, personas, features)   | ✅ Done        |
| 2     | System architecture, database design, AI architecture  | ✅ Done        |
| 3     | UI/UX wireframes, navigation, component hierarchy       | ✅ Done        |
| 4     | Backend development                                     | 🟡 Core done   |
| 5     | Frontend development                                    | 🟡 Core done   |
| 6     | AI agent development                                    | ✅ Done        |
| 7     | Analytics dashboards                                    | ✅ Done        |
| 8     | Testing (80%+ coverage)                                 | 🟡 AI+FE run   |
| 9     | Deployment (Docker, Railway/Render/AWS)                 | ✅ Done        |
| 10    | Documentation (MCA synopsis, SRS, report, viva)         | ⏳ Planned     |

## Phase 1 Documents

- [Problem Statement](docs/phase-01-product/01-problem-statement.md)
- [Market Analysis](docs/phase-01-product/02-market-analysis.md)
- [User Personas](docs/phase-01-product/03-user-personas.md)
- [Feature Breakdown](docs/phase-01-product/04-feature-breakdown.md)
- [Product Requirements Document (PRD)](docs/phase-01-product/05-prd.md)

## Phase 2 Documents

- [Architecture Overview (HLD)](docs/phase-02-architecture/01-architecture-overview.md)
- [Low-Level Design (LLD)](docs/phase-02-architecture/02-low-level-design.md)
- [Database Design](docs/phase-02-architecture/03-database-design.md)
- [PostgreSQL Schema (DDL)](docs/phase-02-architecture/04-schema.sql)
- [AI Architecture](docs/phase-02-architecture/05-ai-architecture.md)
- [Architecture Decision Records](docs/phase-02-architecture/06-adrs.md)

## Phase 3 Documents

- [Design System](docs/phase-03-uiux/01-design-system.md)
- [Information Architecture & Navigation](docs/phase-03-uiux/02-information-architecture.md)
- [Wireframes (Low-Fidelity)](docs/phase-03-uiux/03-wireframes.md)
- [Component Hierarchy & Frontend Structure](docs/phase-03-uiux/04-component-hierarchy.md)

## Phase 4 — Backend (Spring Boot 3 / Java 21)

Runnable API tier under [`backend/`](backend/) — see [backend/README.md](backend/README.md).
Auth + RBAC, cases, documents, async AI analysis orchestration, and the Case Dashboard
read API. 70 Java files; schema via Flyway.

## Phase 5 — Frontend (React + TS + Vite)

Runnable SPA under [`frontend/`](frontend/) — see [frontend/README.md](frontend/README.md).
Auth with silent JWT refresh, role-aware shell, case repository, upload→analyze wizard, and
the Case Analysis Dashboard (overview/timeline/facts/issues/arguments/IRAC) with run polling.
**Typecheck + production build verified clean.**

## Phase 6 — AI Service (FastAPI + LangGraph)

Runnable AI tier under [`ai-service/`](ai-service/) — see [ai-service/README.md](ai-service/README.md).
Document pipeline, the **7-agent graph** (Fact → Issue → Statute/Argument → Precedent/Risk →
Judge → IRAC) emitting the exact `AgentResultsPayload` the backend ingests, plus grounded RAG
chat. **Runs offline via a deterministic mock provider** (real Claude + Qdrant by config).
Agent-graph test + full app import **verified passing**.

---

_LexMind AI — built as an MCA capstone, engineered to production standards._
