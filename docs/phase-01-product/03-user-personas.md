# LexMind AI — User Personas

**Document:** Phase 1 / 03
**Status:** Draft for review
**Owner:** Product / UX
**Last updated:** 2026-06-13

These personas drive feature prioritization, RBAC design, and UX. Each maps to a system
**role** (see [PRD §RBAC](05-prd.md)) and to portal surfaces (Student / Advocate / Research
/ Admin).

---

## Persona 1 — Aarav, the Law Student
**Role:** `LAW_STUDENT` · **Portal:** Student

| | |
|---|---|
| **Age / context** | 21, 3rd-year LLB (5-year integrated), tier-2 city, India |
| **Tech comfort** | High (mobile-first, uses YouTube/Notion to study) |
| **Devices** | Phone primarily, laptop for assignments |

**Goals**
- Understand the *structure* of judgments, not just read them.
- Apply **IRAC** correctly and consistently for moots, internals, and exams.
- Build a personal library of case briefs to revise from.

**Frustrations**
- 50–80 page judgments are intimidating; hard to find the ratio/holding.
- Inconsistent briefs; no feedback on whether he captured what mattered.
- Paid databases are expensive and overkill for learning.

**A day with LexMind**
> Aarav uploads *Kesavananda Bharati*. Within minutes he sees the **Issue**, **Rule**,
> **Application**, **Conclusion**, a **timeline**, the **statutes/articles** involved, and a
> clean **case brief** he can export to study. He chats with the judgment to ask
> "what was the dissent's reasoning?" and gets a grounded answer with citations.

**Success metric:** time-to-understand a new case drops from hours to minutes; higher moot/
exam confidence.

**Key features:** Upload Case, IRAC Dashboard, Case Brief Generator, Timeline Generator,
Research Notes, Legal Research Assistant (chat), Legal Learning Dashboard.

---

## Persona 2 — Adv. Meera, the Solo Advocate
**Role:** `ADVOCATE` · **Portal:** Advocate

| | |
|---|---|
| **Age / context** | 34, district & sessions + High Court practice, ~70 active matters |
| **Tech comfort** | Medium; values speed and reliability over novelty |
| **Devices** | Laptop in chambers, phone in court corridors |

**Goals**
- Walk into each hearing **prepared** without re-reading the entire file.
- Keep a clean, searchable repository of every matter's documents and analysis.
- Anticipate the opponent's arguments and the bench's likely questions.

**Frustrations**
- Files are messy scanned PDFs; reconstructing chronology before each date is painful.
- Evidence and witness status live in her memory and margins.
- Junior turnover means analysis walks out the door.

**A day with LexMind**
> Before a hearing, Meera opens the matter dashboard: an **auto-generated chronology**, a
> **fact matrix** (established/disputed/missing), **evidence** with strength/weakness flags,
> **witness contradictions**, a side-by-side **argument map**, a **judge-perspective**
> question list, and a **hearing checklist** — exported to PDF for court.

**Success metric:** hearing-prep time per matter cut by >60%; fewer "missed" evidence/
contradiction points.

**Key features:** Case Repository, Matter Management, Evidence Analysis, Witness Analysis,
Strategy Dashboard, Hearing Preparation, Risk & Readiness analytics, Cross-Examination
Assistant.

---

## Persona 3 — Dr. Rohan, the Legal Researcher / Academic
**Role:** `RESEARCHER` · **Portal:** Research

| | |
|---|---|
| **Age / context** | 41, law professor + practising researcher, publishes on constitutional law |
| **Tech comfort** | High; comfortable with data and citations |
| **Devices** | Desktop/laptop |

**Goals**
- Trace how a **legal principle evolved** across decades of judgments.
- Build **citation networks** and find landmark/similar cases efficiently.
- Quantify **jurisdiction trends, success rates, subject-matter patterns**.

**Frustrations**
- Citation tracing is manual and fragmented across paywalled tools.
- Hard to quantify trends without scraping/spreadsheets.

**A day with LexMind**
> Rohan runs a **similar-case discovery** on a seed judgment, gets a **ranked, grounded**
> precedent list and a **citation network** graph, and exports a **legal-trend** view
> (court patterns, subject-matter frequency) for his paper.

**Success metric:** research that took days compressed to hours; reproducible, citable
outputs.

**Key features:** Citation Analysis, Similar Case Discovery, Legal Trend Analysis, Research
Workspace, Research Intelligence Dashboard.

---

## Persona 4 — Sana, the Law Firm Admin / Managing Partner
**Role:** `LAW_FIRM_ADMIN` · **Portal:** Advocate (firm-scoped) + Admin (firm tenant)

| | |
|---|---|
| **Age / context** | 47, managing partner of a 12-lawyer litigation boutique |
| **Tech comfort** | Medium; cares about ROI, security, and team output |
| **Devices** | Laptop |

**Goals**
- One source of truth for **case status, readiness, and analytics** across the team.
- Objective **readiness/risk scoring** per matter to allocate effort.
- Secure, role-scoped access; onboard associates onto matters fast.

**Frustrations**
- No portfolio-level visibility; status lives in individual lawyers' heads.
- Onboarding associates onto a matter is slow and expensive.
- Worries about confidentiality and access control.

**A day with LexMind**
> Sana reviews the **Analytics Center**: readiness scores across the portfolio, risk flags,
> upcoming hearings, and team workload. She assigns an associate to a matter with scoped
> access; the associate is productive in minutes thanks to the existing dashboards.

**Success metric:** portfolio visibility; faster associate onboarding; defensible access
control and audit trail.

**Key features:** Team workspace, Analytics Center, User/seat management (firm-scoped),
Audit visibility, Litigation Readiness Dashboard.

---

## Persona 5 — Vikram, the Super Admin (Platform Operator)
**Role:** `SUPER_ADMIN` · **Portal:** Admin

| | |
|---|---|
| **Context** | LexMind AI platform operations / DevOps + support |
| **Tech comfort** | Expert |

**Goals**
- Keep the platform healthy, secure, and compliant.
- Monitor **AI usage/cost/quality**, document-processing health, and user activity.
- Manage users, roles, tenants; review **audit logs**; respond to incidents.

**Frustrations (designed away)**
- Needs full observability into AI calls, failures, and costs.
- Needs airtight RBAC and tamper-evident audit logs.

**A day with LexMind**
> Vikram checks the **AI Monitoring** dashboard (latency, token cost, agent error rates),
> the **Document Monitoring** queue (OCR/parse failures), platform **Analytics**, and
> **Audit Logs**; manages a flagged account; reviews a security alert.

**Key features:** User Management, AI Monitoring, Document Monitoring, Platform Analytics,
Audit Logs.

---

## Persona Comparison Matrix

| Dimension | Aarav (Student) | Meera (Advocate) | Rohan (Researcher) | Sana (Firm Admin) | Vikram (Super Admin) |
|---|---|---|---|---|---|
| **Primary job** | Learn case structure | Prepare & strategize matters | Research & trends | Manage portfolio | Operate platform |
| **Top value** | IRAC + brief | Hearing readiness | Precedent + analytics | Visibility + control | Health + compliance |
| **Volume of docs** | Low–medium | High | Medium | Portfolio (high) | N/A |
| **Willingness to pay** | Low | Medium | Medium-high | High (seats) | N/A (operator) |
| **Sensitivity to AI errors** | Medium | High | High | High | Critical |
| **Killer feature** | IRAC Dashboard | Hearing Prep + Strategy | Citation Network | Analytics Center | Audit + AI Monitoring |

---

## Anti-Personas (Explicitly Not Targeting in v1)

- **In-house corporate counsel doing contract lifecycle management** — different workflow
  (drafting/CLM), addressed by other tools; not the litigation-analysis wedge.
- **Pro-se litigants seeking legal advice** — liability and "no legal advice" boundary make
  this out of scope for v1.
- **Large-firm enterprise procurement** — high-touch security/integration requirements;
  deferred to a later phase.

---

_Previous: [← Market Analysis](02-market-analysis.md) · Next: [Feature Breakdown →](04-feature-breakdown.md)_
