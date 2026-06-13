# LexMind AI — Feature Breakdown

**Document:** Phase 1 / 04
**Status:** Draft for review
**Owner:** Product
**Last updated:** 2026-06-13

This document enumerates every feature, organizes it into modules, prioritizes with
**MoSCoW**, and defines a **release plan** (MVP → v1 → v2). Each feature carries an ID used
downstream in the SRS, API design, and traceability matrix (Phase 10).

**Legend — MoSCoW:** 🟥 **M**ust · 🟧 **S**hould · 🟨 **C**ould · ⬜ **W**on't (this release)
**Release:** `MVP` (Phases 4–7 core) · `v1` (post-MVP hardening) · `v2` (later)
**Roles:** ST=Student, AD=Advocate, RS=Researcher, FA=Firm Admin, SA=Super Admin

---

## 0. Feature Map (Modules)

```
LexMind AI
├── F0  Platform & Auth (RBAC, accounts, tenancy)
├── F1  Document Ingestion (upload, OCR, parse, classify)
├── F2  Case Workspace (matters, repository, organization)
├── F3  Case Analysis Dashboard
│        ├── Case Overview        ├── Fact Matrix
│        ├── Timeline             ├── Legal Issues
│        ├── Statutes             ├── Party Arguments
│        ├── Evidence             ├── Witnesses
│        └── Precedents
├── F4  IRAC Dashboard (education)
├── F5  Legal Analytics Center
│        ├── Case Strength   ├── Risk   ├── Litigation Readiness
│        ├── Research Intelligence   └── Legal Trends
├── F6  AI Agents (7 core agents)
├── F7  Advanced AI Assistants (brief, chat/RAG, hearing, cross-exam, strategy)
├── F8  Research Portal (citation, similar-case, trends, workspace)
├── F9  Admin & Observability (users, AI/doc monitoring, audit, analytics)
└── F10 Cross-cutting (search, export, notifications, theming, PDF viewer)
```

---

## 1. F0 — Platform & Authentication

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F0.1 | Register / Login / Logout (JWT) | all | 🟥 M | MVP |
| F0.2 | Forgot / reset password (email token) | all | 🟥 M | MVP |
| F0.3 | RBAC enforcement (role + permission matrix) | all | 🟥 M | MVP |
| F0.4 | User profile & settings | all | 🟥 M | MVP |
| F0.5 | Firm tenant + seat management | FA, SA | 🟧 S | v1 |
| F0.6 | Email verification | all | 🟧 S | v1 |
| F0.7 | SSO / OAuth (Google) | all | 🟨 C | v2 |
| F0.8 | 2FA / MFA | AD, FA, SA | 🟨 C | v2 |

## 2. F1 — Document Ingestion

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F1.1 | Secure upload (PDF/DOCX/images), validation, virus/type checks | ST, AD, RS | 🟥 M | MVP |
| F1.2 | OCR for scanned PDFs/images (Tesseract) | ST, AD, RS | 🟥 M | MVP |
| F1.3 | PDF/DOCX text + structure parsing | all | 🟥 M | MVP |
| F1.4 | Document-type classification (FIR, judgment, petition, affidavit, charge sheet, contract, notice, written statement) | all | 🟥 M | MVP |
| F1.5 | Metadata extraction (court, case no., dates, parties) | all | 🟥 M | MVP |
| F1.6 | Async processing pipeline + status (queued/processing/done/failed) | all | 🟥 M | MVP |
| F1.7 | Multi-document matter assembly | AD, RS | 🟧 S | v1 |
| F1.8 | Re-process / re-OCR on demand | AD | 🟨 C | v2 |

## 3. F2 — Case Workspace

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F2.1 | Create / view / edit / archive case (matter) | ST, AD, RS | 🟥 M | MVP |
| F2.2 | Case repository list with filters/search | AD, RS | 🟥 M | MVP |
| F2.3 | Matter management (stage, court, parties, dates) | AD, FA | 🟧 S | v1 |
| F2.4 | Document annotation / highlighting | ST, AD | 🟨 C | v2 |
| F2.5 | Team assignment & sharing (firm-scoped) | FA, AD | 🟧 S | v1 |

## 4. F3 — Case Analysis Dashboard

| ID | Feature | Source (AI agent) | Roles | MoSCoW | Release |
|---|---|---|---|---|---|
| F3.1 | **Case Overview** (name, court, jurisdiction, case no., filing date, stage, parties) | Fact Extraction | all | 🟥 M | MVP |
| F3.2 | **Case Timeline** (auto chronology, visual) | Fact Extraction | all | 🟥 M | MVP |
| F3.3 | **Fact Matrix** (established / disputed / missing) | Fact Extraction + Risk | all | 🟥 M | MVP |
| F3.4 | **Legal Issue Dashboard** (primary/secondary, ranked) | Issue Identification | all | 🟥 M | MVP |
| F3.5 | **Statute Dashboard** (applicable laws/provisions mapped) | Statute Analysis | all | 🟥 M | MVP |
| F3.6 | **Party Argument Dashboard** (petitioner vs respondent, side-by-side) | Argument Builder | AD, ST | 🟧 S | MVP |
| F3.7 | **Evidence Dashboard** (classify, strength, relevance, weakness, gaps) | Risk + Evidence | AD | 🟧 S | v1 |
| F3.8 | **Witness Dashboard** (statements, contradictions, corroborations, reliability) | Risk + Witness | AD | 🟧 S | v1 |
| F3.9 | **Precedent Dashboard** (similar/landmark cases, citation links, ranked) | Precedent Research | all | 🟧 S | v1 |

## 5. F4 — IRAC Dashboard (Education)

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F4.1 | Auto IRAC: Issue / Rule / Application / Conclusion per case | ST | 🟥 M | MVP |
| F4.2 | Per-issue IRAC breakdown (multiple issues) | ST | 🟧 S | v1 |
| F4.3 | Editable / student-annotated IRAC | ST | 🟨 C | v2 |

## 6. F5 — Legal Analytics Center

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F5.1 | **Case Strength** (strong/weak args, missing evidence, open questions) | AD, FA | 🟧 S | v1 |
| F5.2 | **Risk Analysis** (procedural, evidentiary, jurisdiction, documentation) | AD, FA | 🟧 S | v1 |
| F5.3 | **Litigation Readiness** score (evidence/witness/research/hearing) | AD, FA | 🟧 S | v1 |
| F5.4 | **Research Intelligence** (frequently cited, citation networks, principles, jurisdiction trends) | RS | 🟨 C | v2 |
| F5.5 | **Legal Trends** (categories, success rates, court patterns, subject-matter) | RS, FA | 🟨 C | v2 |

## 7. F6 — AI Agents (Core Engine)

| ID | Agent | Output | MoSCoW | Release |
|---|---|---|---|---|
| F6.1 | **Fact Extraction Agent** | facts, dates, parties, events | 🟥 M | MVP |
| F6.2 | **Issue Identification Agent** | ranked legal issues | 🟥 M | MVP |
| F6.3 | **Statute Analysis Agent** | relevant statutes/provisions + applicability | 🟥 M | MVP |
| F6.4 | **Precedent Research Agent** | similar judgments + relevance | 🟧 S | v1 |
| F6.5 | **Argument Builder Agent** | petitioner & respondent arguments | 🟧 S | MVP |
| F6.6 | **Risk Analysis Agent** | weaknesses, contradictions, missing docs | 🟧 S | v1 |
| F6.7 | **Judge Perspective Agent** | key questions, concerns, critical issues | 🟧 S | v1 |

> Agents are orchestrated with **LangGraph** (see Phase 2 AI architecture). MVP ships
> F6.1–F6.3 + F6.5; v1 adds F6.4, F6.6, F6.7.

## 8. F7 — Advanced AI Assistants

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F7.1 | **Case Brief Generator** (judgment → structured brief) | ST, AD | 🟥 M | MVP |
| F7.2 | **Legal Research Assistant** (RAG chat over uploaded case) | all | 🟥 M | MVP |
| F7.3 | **Hearing Preparation Assistant** (notes, checklist, key arguments) | AD | 🟧 S | v1 |
| F7.4 | **Cross-Examination Assistant** (witness questions, contradiction points) | AD | 🟨 C | v2 |
| F7.5 | **Litigation Strategy Assistant** (strongest/weakest, further research) | AD, FA | 🟨 C | v2 |

## 9. F8 — Research Portal

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F8.1 | Citation analysis | RS | 🟨 C | v2 |
| F8.2 | Similar-case discovery (vector search) | RS, AD | 🟧 S | v1 |
| F8.3 | Legal-trend analysis | RS | 🟨 C | v2 |
| F8.4 | Research workspace + notes | RS, ST | 🟧 S | v1 |

## 10. F9 — Admin & Observability

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F9.1 | User management (CRUD, roles, suspend) | SA, FA | 🟥 M | MVP |
| F9.2 | Audit logs (tamper-evident, queryable) | SA, FA | 🟥 M | MVP |
| F9.3 | AI monitoring (latency, cost/tokens, error rates) | SA | 🟧 S | v1 |
| F9.4 | Document monitoring (pipeline queue/failures) | SA | 🟧 S | v1 |
| F9.5 | Platform analytics dashboard | SA | 🟨 C | v2 |

## 11. F10 — Cross-Cutting

| ID | Feature | Roles | MoSCoW | Release |
|---|---|---|---|---|
| F10.1 | Global search | all | 🟧 S | v1 |
| F10.2 | Advanced filters | AD, RS | 🟧 S | v1 |
| F10.3 | Export (PDF/DOCX) of dashboards/briefs | all | 🟥 M | MVP |
| F10.4 | Dark / light mode + responsive design | all | 🟥 M | MVP |
| F10.5 | Interactive charts (Recharts) | all | 🟥 M | MVP |
| F10.6 | In-app PDF viewer | ST, AD | 🟧 S | v1 |
| F10.7 | Notifications (processing done, hearing reminders) | AD | 🟨 C | v2 |
| F10.8 | Public website (Home, About, Features, How It Works, Blog, Resources, Contact) | public | 🟧 S | MVP |

---

## 12. MVP Definition ("Thin Vertical Slice")

The MVP must prove the **core loop end to end**:

> **Upload a document → AI analyzes it → user sees a structured legal intelligence
> dashboard + IRAC + case brief → can chat with the case → can export.**

**MVP must-haves (the demo-able product):**
- F0.1–F0.4 (auth + RBAC), F1.1–F1.6 (ingestion+OCR+classify), F2.1–F2.2 (case workspace)
- F3.1–F3.5 + F3.6 (overview, timeline, fact matrix, issues, statutes, arguments)
- F4.1 (IRAC), F6.1–F6.3 + F6.5 (core agents), F7.1–F7.2 (brief + RAG chat)
- F9.1–F9.2 (user mgmt + audit), F10.3–F10.5 + F10.8 (export, theming, charts, public site)

**Deferred to v1/v2:** evidence/witness/precedent dashboards, analytics center, hearing/
cross-exam/strategy assistants, research portal depth, firm tenancy, SSO/2FA.

---

## 13. Release Plan

| Release | Theme | Headline features |
|---|---|---|
| **MVP** | Prove the core loop | Ingestion+OCR, core dashboards, IRAC, brief, RAG chat, export, auth/RBAC, public site |
| **v1** | Practice value + analytics | Evidence/witness/precedent, analytics center (strength/risk/readiness), hearing prep, similar-case discovery, firm tenancy, AI/doc monitoring |
| **v2** | Depth + collaboration | Cross-exam & strategy assistants, research portal (citation/trends), annotations, SSO/2FA, notifications, platform analytics |

---

## 14. Feature → Persona Traceability (summary)

| Feature module | Aarav (ST) | Meera (AD) | Rohan (RS) | Sana (FA) | Vikram (SA) |
|---|:---:|:---:|:---:|:---:|:---:|
| Ingestion (F1) | ✓ | ✓ | ✓ | ✓ | ⚙ |
| Case Analysis (F3) | ✓ | ✓ | ✓ | ✓ | |
| IRAC (F4) | ★ | ✓ | | | |
| Analytics (F5) | | ✓ | ✓ | ★ | |
| AI Agents (F6) | ✓ | ✓ | ✓ | ✓ | ⚙ |
| Assistants (F7) | ✓ | ★ | | ✓ | |
| Research (F8) | ✓ | ✓ | ★ | | |
| Admin/Obs (F9) | | | | ✓ | ★ |

★ = killer feature for that persona · ✓ = used · ⚙ = operates/monitors

---

_Previous: [← User Personas](03-user-personas.md) · Next: [PRD →](05-prd.md)_
