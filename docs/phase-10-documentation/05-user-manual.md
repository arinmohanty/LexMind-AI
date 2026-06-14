# User Manual

## LexMind AI

**Version:** 1.0 · **Date:** 2026-06-14

> ⚖️ LexMind AI provides **legal analysis, not legal advice.** Always apply professional judgment.

---

## 1. Getting Started

### 1.1 Create an account
1. Open the app (e.g. `http://localhost:8081`). Click **Sign up**.
2. Enter your name, email, password; choose your **role** (Law Student / Advocate / Researcher /
   Law Firm Admin). Firm admins also enter a firm name.
3. Accept the terms + "not legal advice" acknowledgement → **Create account**. You're logged in.

### 1.2 Log in / out
Use **Log in** with your email + password. Use the **Logout** button in the top bar. Forgot your
password? Use **Forgot password** to receive a reset link.

### 1.3 The workspace
A left **sidebar** (role-specific) and a top **app bar** (your name/role, theme toggle, logout).
Switch **dark/light** with the sun/moon icon.

---

## 2. Analyzing a Case (core workflow)

1. **Upload a case:** click **Upload Case** (sidebar) or **New Case**.
2. **Step 1 — Case details:** enter a title (and optionally type, court, case number) → **Create case**.
3. **Step 2 — Upload documents:** drag/click to add FIRs, judgments, petitions, contracts, etc.
   (PDF, DOCX, images — OCR is applied automatically). Watch each file's status (queued →
   processing → done).
4. Click **Analyze case.** You're taken to the **Case Analysis Dashboard**.
5. A status banner shows the analysis progressing; tabs **fill in as agents finish**.

---

## 3. The Case Analysis Dashboard

A tab bar across the matter:

| Tab | What you see |
|---|---|
| **Overview** | Case snapshot, parties, and quick stats (fact/issue/argument counts). |
| **Timeline** | Auto-generated chronology of events (incident → FIR → … → judgment). |
| **Facts** | Fact matrix: **Established / Disputed / Missing**, each with confidence; filter by status. |
| **Issues** | Ranked legal issues (primary/secondary). |
| **Arguments** | Petitioner vs respondent arguments, side-by-side, with strength meters. |
| **IRAC** | Issue / Rule / Application / Conclusion breakdown. |
| **Analytics** | Litigation-readiness gauge + sub-scores, case-strength findings, and risk list. |

**Re-run** re-analyzes the case (replaces prior results). **Export** (where enabled) downloads the
dashboard/brief. Every AI section shows a **"not legal advice"** banner.

---

## 4. Chatting with a Case (RAG)

Open a case's chat, ask a question (e.g. *"What time did PW1 say the incident occurred?"*). The
assistant answers **using only your documents**, with **clickable citations** and a confidence
indicator. If the answer isn't in the documents, it says so.

---

## 5. Analytics Center (Advocate / Firm)

The **Analytics** item shows a portfolio view: number of cases, average readiness, high-risk count,
a **readiness-by-matter** bar chart, and a **risk-by-type** donut. Click a matter to open its
analytics.

---

## 6. Roles & What You Can Do

| You are… | You can… |
|---|---|
| **Law Student** | Upload cases, view dashboards, IRAC, briefs, chat, research. |
| **Advocate** | All of the above + evidence/witness/strategy, analytics, hearing prep. |
| **Researcher** | Cases + citation/similar-case/trend research. |
| **Firm Admin** | Firm-scoped matters + team + portfolio analytics. |
| **Super Admin** | User management, AI/document monitoring, audit logs. |

You can only access **your own** cases (firm admins: your firm's). Attempting to open another
user's case shows "not found".

---

## 7. Settings
Update your profile, change password, switch theme (light/dark/system) and density. Firm admins
manage team and seats.

---

## 8. Troubleshooting

| Symptom | What to do |
|---|---|
| Tabs are empty after upload | Analysis may still be running — wait for the status banner to complete, or click **Re-run**. |
| "Analysis failed" | The AI service may be unavailable; retry. Dashboards already computed remain visible. |
| Upload rejected | Check file type (PDF/DOCX/PNG/JPG/TIFF) and size limit (25 MB default). |
| Logged out unexpectedly | Your session expired; log in again. |

---

## 9. Important Notes
- Outputs are **AI-generated analysis**; verify against primary sources.
- Keep documents confidential; access is role-scoped and audited.
- LexMind AI does **not** file in court, give advice, or guarantee outcomes.
