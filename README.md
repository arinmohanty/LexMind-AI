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
| 1     | Product foundation (PRD, market, personas, features)   | 🟡 In progress |
| 2     | System architecture, database design, AI architecture  | ⏳ Planned     |
| 3     | UI/UX wireframes, navigation, component hierarchy       | ⏳ Planned     |
| 4     | Backend development                                     | ⏳ Planned     |
| 5     | Frontend development                                    | ⏳ Planned     |
| 6     | AI agent development                                    | ⏳ Planned     |
| 7     | Analytics dashboards                                    | ⏳ Planned     |
| 8     | Testing (80%+ coverage)                                 | ⏳ Planned     |
| 9     | Deployment (Docker, Railway/Render/AWS)                 | ⏳ Planned     |
| 10    | Documentation (MCA synopsis, SRS, report, viva)         | ⏳ Planned     |

## Phase 1 Documents

- [Problem Statement](docs/phase-01-product/01-problem-statement.md)
- [Market Analysis](docs/phase-01-product/02-market-analysis.md)
- [User Personas](docs/phase-01-product/03-user-personas.md)
- [Feature Breakdown](docs/phase-01-product/04-feature-breakdown.md)
- [Product Requirements Document (PRD)](docs/phase-01-product/05-prd.md)

---

_LexMind AI — built as an MCA capstone, engineered to production standards._
