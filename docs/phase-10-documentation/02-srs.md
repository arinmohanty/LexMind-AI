# Software Requirements Specification (SRS)

## LexMind AI — Legal Intelligence, Case Analysis & Litigation Strategy Platform

**Version:** 1.0 · **Standard:** based on IEEE 830 · **Date:** 2026-06-14

---

## 1. Introduction

### 1.1 Purpose
This SRS specifies the functional and non-functional requirements of **LexMind AI**, a web
platform that converts legal documents into structured legal-intelligence dashboards. It is
intended for the development team, guide/evaluators, and future maintainers.

### 1.2 Scope
LexMind AI ingests legal documents, runs a multi-agent AI analysis, and presents results as a
Case Analysis Dashboard (facts, timeline, issues, statutes, arguments, IRAC), plus analytics
(strength/risk/readiness) and grounded RAG chat. It enforces RBAC across five roles and logs
all mutating actions. It does **not** provide legal advice.

### 1.3 Definitions, Acronyms, Abbreviations
| Term | Meaning |
|---|---|
| IRAC | Issue, Rule, Application, Conclusion |
| RAG | Retrieval-Augmented Generation |
| RBAC | Role-Based Access Control |
| JWT | JSON Web Token |
| OCR | Optical Character Recognition |
| LLM | Large Language Model |
| Matter / Case | A unit of legal work: documents + analysis |

### 1.4 References
PRD, Architecture, ER/Schema, AI architecture, ADRs (see [`docs/`](../)); IEEE 830-1998.

### 1.5 Overview
Section 2 gives the overall description; Section 3 lists specific requirements (functional,
external interface, non-functional); Section 4 lists use cases.

---

## 2. Overall Description

### 2.1 Product Perspective
A new, self-contained, three-tier system: React SPA → Spring Boot REST API (system of record:
PostgreSQL) → FastAPI AI service (LangGraph agents, RAG; vector store: Qdrant). Provider-
abstracted LLM (default Claude). Containerized with Docker Compose.

### 2.2 Product Functions (summary)
Registration/login, document upload + OCR, case management, AI analysis (7 agents), dashboards
(overview/timeline/facts/issues/statutes/arguments/IRAC), case brief, RAG chat, analytics
(strength/risk/readiness), admin (users/audit/monitoring), exports, theming.

### 2.3 User Classes and Characteristics
| Role | Characteristics |
|---|---|
| Law Student | Learning-focused; IRAC, briefs; mobile-first |
| Advocate | Practice-focused; many matters; readiness, strategy |
| Researcher | Citation/precedent/trend analysis |
| Law Firm Admin | Firm-scoped management + analytics |
| Super Admin | Platform operations, monitoring, audit |

### 2.4 Operating Environment
Modern browsers (desktop/tablet/mobile). Server: Docker on Linux; JDK 21, Python 3.12, Node 20;
PostgreSQL 16; Qdrant.

### 2.5 Design & Implementation Constraints
No legal advice; grounded AI with citations; India-first English legal context; data privacy
(DPDP-aligned); stateless JWT; 12-factor configuration.

### 2.6 Assumptions and Dependencies
Users supply their own documents; a capable LLM provider is available (or the mock provider for
demos); public judgments may seed a precedent corpus.

---

## 3. Specific Requirements

### 3.1 Functional Requirements
IDs map to the [Feature Breakdown](../phase-01-product/04-feature-breakdown.md).

| ID | Requirement | Priority |
|---|---|---|
| FR-1 | The system shall allow users to register with a role and authenticate via JWT. | Must |
| FR-2 | The system shall support password reset via emailed token. | Should |
| FR-3 | The system shall enforce RBAC per the permission matrix at the API. | Must |
| FR-4 | The system shall let authorized users create and manage cases (matters). | Must |
| FR-5 | The system shall accept document uploads (PDF/DOCX/image) with validation. | Must |
| FR-6 | The system shall extract text via OCR/parsing and store metadata. | Must |
| FR-7 | The system shall run an asynchronous 7-agent AI analysis on a case. | Must |
| FR-8 | The system shall present a Case Analysis Dashboard: overview, timeline, fact matrix (established/disputed/missing), ranked issues, applicable statutes, side-by-side arguments. | Must |
| FR-9 | The system shall generate an IRAC analysis per case. | Must |
| FR-10 | The system shall generate a structured case brief. | Should |
| FR-11 | The system shall provide grounded RAG chat over a case's documents with citations. | Must |
| FR-12 | The system shall compute analytics: case strength, risk assessments, readiness scores. | Should |
| FR-13 | The system shall allow export of dashboards/briefs (PDF/DOCX). | Should |
| FR-14 | The system shall record all mutating actions in an audit log. | Must |
| FR-15 | The system shall provide admin functions: user management, audit read, AI/document monitoring. | Should |
| FR-16 | The system shall isolate data per user/firm (a user cannot access another's case). | Must |
| FR-17 | The system shall surface AI outputs as analysis with a persistent "not legal advice" notice. | Must |

### 3.2 External Interface Requirements
- **UI:** responsive SPA; dark/light; keyboard-navigable; WCAG-AA-leaning.
- **API:** REST under `/api/v1`, JSON envelope `{data,error,traceId}`, documented via OpenAPI/Swagger.
- **Service interface:** backend ↔ AI service over REST with an internal service token.
- **External services:** LLM/embedding provider; SMTP; object storage (local/S3).

### 3.3 Non-Functional Requirements
| ID | Category | Requirement |
|---|---|---|
| NFR-1 | Performance | Non-AI API < 300 ms p95; AI analysis async with progressive results. |
| NFR-2 | Scalability | Stateless API (horizontal scale); async AI workers; independent vector store. |
| NFR-3 | Security | JWT, RBAC, input validation, rate limiting, secure uploads, OWASP Top-10 controls, TLS, encryption at rest. |
| NFR-4 | Reliability of AI | Grounded outputs with citations; low-confidence flagged; no fabricated authorities. |
| NFR-5 | Availability | 99.5% target; graceful degradation if AI tier is down. |
| NFR-6 | Usability/Accessibility | Responsive; dark/light; AA-leaning contrast/focus/keyboard. |
| NFR-7 | Maintainability | Modular monorepo; typed contracts; ≥80% test-coverage target; CI. |
| NFR-8 | Portability | Dockerized; local + Railway/Render/AWS. |
| NFR-9 | Auditability/Privacy | Append-only audit; tenant isolation; data deletion support. |

---

## 4. Use Cases (selected)

### UC-1 Analyze a Case
**Actor:** Advocate/Student · **Pre:** authenticated, case created.
1. Actor uploads documents. 2. System validates + stores (status QUEUED). 3. Actor clicks Analyze.
4. System creates an analysis run (202) and dispatches to the AI tier. 5. AI runs the 7 agents,
returns structured results. 6. System persists results; actor's dashboard fills progressively.
**Post:** dashboard populated; run COMPLETED/PARTIAL.
**Alt:** AI tier unreachable → run FAILED; dashboards remain viewable; retry available.

### UC-2 Chat with a Case (RAG)
Actor asks a question → system retrieves case-scoped chunks → LLM answers with citations →
answer persisted and shown with clickable sources; "not in documents" if unsupported.

### UC-3 Enforce Access Control
Actor requests another user's case → system returns 404 (existence hidden). Unauthenticated
request → 401. Insufficient permission → 403.

### UC-4 View Analytics
Actor opens the Analytics tab/Center → system returns case strength, risks, and readiness (per
case) or portfolio aggregates (avg readiness, high-risk count, readiness-by-matter, risk-by-type).

---

## 5. Traceability
Each FR traces to a feature ID (Phase 1), a controller/endpoint (Phase 4 backend / Phase 6 AI),
a UI surface (Phase 3/5), and a test (Phase 8). The mapping is maintained in the
[Final Report](08-final-report.md#traceability).
