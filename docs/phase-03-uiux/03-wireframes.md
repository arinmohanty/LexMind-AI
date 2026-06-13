# LexMind AI — Wireframes (Low-Fidelity)

**Document:** Phase 3 / 03
**Status:** Draft for review
**Owner:** UI/UX Design
**Last updated:** 2026-06-14

> Low-fi layout blueprints (ASCII) for the key screens. They define structure, hierarchy, and
> the placement of LexMind domain components — not final visuals. High-fi visuals are produced
> in Phase 5 from the [Design System](01-design-system.md). Legend: `[ ]` button · `____`
> input · `�$` chart · `●` status dot · `⌕` search · `▾` dropdown.

---

## 1. Public Landing (`/`)

```
┌───────────────────────────────────────────────────────────────────────────┐
│  ⚖ LexMind AI        Features  How it Works  Resources  Blog   [Login][Sign up]│
├───────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│     Upload a case. Get a complete legal intelligence dashboard.             │
│     AI-powered case analysis for students, advocates & researchers.         │
│                                                                             │
│              [ Start free ]   [ See how it works ▸ ]                         │
│                                                                             │
│     ┌─────────────────────────────────────────────────────────────────┐    │
│     │  (product screenshot: Case Analysis Dashboard)                  │    │
│     └─────────────────────────────────────────────────────────────────┘    │
├───────────────────────────────────────────────────────────────────────────┤
│  ▣ Fact Matrix   ▣ IRAC   ▣ Statutes   ▣ Evidence   ▣ Precedents   ▣ Risk   │
│  "From FIR to judgment — structured in minutes."                            │
├───────────────────────────────────────────────────────────────────────────┤
│  For Students │ For Advocates │ For Researchers │ For Firms   (tabbed value) │
├───────────────────────────────────────────────────────────────────────────┤
│  How it works:  1 Upload → 2 AI analyzes → 3 Explore dashboards → 4 Export   │
├───────────────────────────────────────────────────────────────────────────┤
│  ⚠ LexMind provides legal analysis, not legal advice.                       │
│  Footer: Product · Company · Resources · Legal (Terms/Privacy) · Contact     │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Login (`/login`) · Register (`/register`)

```
┌──────────────────────────────┬────────────────────────────────────────────┐
│  ⚖  LexMind AI               │   Welcome back                              │
│                              │                                            │
│  "Legal intelligence,        │   Email     ______________________         │
│   structured."               │   Password  ______________________  ◌      │
│                              │   [✓] Remember me        Forgot password?  │
│  • Case Analysis Dashboards  │                                            │
│  • IRAC & Case Briefs        │           [   Log in   ]                   │
│  • Grounded RAG chat         │   ───────────  or  ───────────             │
│                              │   New here?  Create an account ▸           │
│  (brand panel / illustration)│                                            │
└──────────────────────────────┴────────────────────────────────────────────┘

Register adds: Full name, Role ▾ (Student/Advocate/Researcher/Firm), Org (if firm),
confirm password, [✓] I accept Terms + "no legal advice" acknowledgement.
```

---

## 3. App Shell (all authenticated screens)

```
┌──────────┬────────────────────────────────────────────────────────────────┐
│ ⚖ LexMind│  Cases / Sharma v. State / Analysis        ⌕⌘K   ☾  🔔  (AB ▾)  │ ← app bar
│──────────│────────────────────────────────────────────────────────────────│
│ ◧ Dashbd │                                                                │
│ ▤ Cases  │                                                                │
│ ◎ IRAC   │                 << SCREEN CONTENT >>                            │
│ ✎ Briefs │                                                                │
│ ⌕ Resrch │                                                                │
│ ▦ Analyt │                                                                │
│ ⤓ Reports│                                                                │
│ ⚙ Settngs│                                                                │
│  [+ Upload]                                                               │
└──────────┴────────────────────────────────────────────────────────────────┘
Sidebar items vary by role (see IA §3). < lg collapses to icon rail; < md → drawer.
```

---

## 4. Role Dashboard (`/app/dashboard`) — Advocate example

```
┌────────────────────────────────────────────────────────────────────────────┐
│  Good morning, Adv. Meera            [+ New Matter]  [+ Upload]             │
├───────────────┬───────────────┬───────────────┬────────────────────────────┤
│ Active Matters│ Hearings (7d)  │ Avg Readiness │ Open Risks                 │
│      72       │      4         │     63% �•     │     11 ●                    │
├───────────────┴───────────────┴───────────────┴────────────────────────────┤
│  Upcoming hearings                         │  Recently analyzed             │
│  ─ Mon 16  Sharma v. State  ● 58% ready    │  ─ Verma v. Verma   2h ago     │
│  ─ Tue 17  ABC Pvt Ltd      ● 71% ready    │  ─ State v. Khan    1d ago     │
│  ─ Thu 19  Patel matter     ● 40% ready ⚠  │  ─ Contract dispute 2d ago     │
├────────────────────────────────────────────┴────────────────────────────────┤
│  Portfolio readiness �$ (bar)        │  Risk distribution �$ (donut)          │
└────────────────────────────────────────────────────────────────────────────┘
Student dashboard variant: "Continue learning", recent IRAC, sample cases, study streak.
```

---

## 5. Case Repository (`/app/cases`)

```
┌────────────────────────────────────────────────────────────────────────────┐
│  Case Repository                                   [+ New Matter] [+ Upload] │
│  ⌕ Search cases…__________   Type▾  Court▾  Stage▾  Status▾   ▦ grid │ ☰ list │
├────────────────────────────────────────────────────────────────────────────┤
│  Title                 Case No.     Court        Stage     Ready  Updated   │
│  ─ Sharma v. State     CRL/452/24   HC Delhi     Trial     ●58%   2h        │
│  ─ ABC Pvt Ltd v. XYZ  CS/118/24    Dist. Court  Pleadings ●71%   1d        │
│  ─ Verma v. Verma      HMA/77/23    Family Court Arguments ●66%   1d        │
│  ─ State v. Khan       SC/12/24     Sessions     Judgment  ●82%   3d        │
│  … pagination ‹ 1 2 3 ›                                                      │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 6. Upload / Create Case (`/app/cases/new`)

```
┌────────────────────────────────────────────────────────────────────────────┐
│  New Case                                                       Step 1 of 2  │
│  Title ____________________   Type ▾   Court ____________  Case No. ________ │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │   ⤒  Drag & drop FIR, judgment, petition, contract…                  │  │
│  │      or [ Browse ]   PDF · DOCX · images (OCR auto)                   │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
│  Uploaded:                                                                  │
│   ● judgment.pdf      12 pg   ✓ parsed                                       │
│   ◐ fir_scan.jpg              OCR… 60%                                       │
│   ◌ petition.docx             queued                                         │
│                                          [ Cancel ]   [ Analyze case ▸ ]     │
└────────────────────────────────────────────────────────────────────────────┘
After "Analyze": ProcessingState with per-agent progress (Facts ✓ · Issues ◐ · Statutes ◌ …)
```

---

## 7. Case Analysis Dashboard (`/app/cases/:id/analysis`)

```
┌────────────────────────────────────────────────────────────────────────────┐
│  Sharma v. State  · CRL/452/24 · HC Delhi · Trial      [⤓ Export] [↻ Re-run] │
│  Overview │ Timeline │ Facts │ Issues │ Statutes │ Arguments │ Evidence │ …  │ ← tabs
│  ⚠ AI analysis — not legal advice.                          Last run: 2h ago │
├──────────────────────────────── OVERVIEW ──────────────────────────────────┤
│ ┌── Case Snapshot ──────────────┐ ┌── Parties ─────────────────────────────┐│
│ │ Court:     HC Delhi           │ │ Petitioner: R. Sharma  (Adv. Meera)    ││
│ │ Case No.:  CRL/452/24         │ │ Respondent: State of Delhi (PP)        ││
│ │ Type:      Criminal           │ └────────────────────────────────────────┘│
│ │ Stage:     Trial              │ ┌── Quick Stats ─────────────────────────┐│
│ │ Filed:     12 Jan 2024        │ │ Facts 18 (●9 ●6 ●3) Issues 4 Statutes 7││
│ └───────────────────────────────┘ │ Evidence 12  Witnesses 5  Precedents 9 ││
│                                    └────────────────────────────────────────┘│
│ ┌── Mini Timeline ───────────────────────────────────────────────────────┐ │
│ │ Incident ─▶ FIR ─▶ Investigation ─▶ Charge Sheet ─▶ Trial               │ │
│ └────────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────────┘
```

### 7a. Facts tab (Fact Matrix)
```
│ [● Established 9] [● Disputed 6] [● Missing 3]      ⌕ filter facts…          │
│ ───────────────────────────────────────────────────────────────────────────│
│ ● Established  Accused was present at scene on 12 Jan       conf .92  ⟦cite⟧ │
│ ● Disputed     Weapon belonged to accused                   conf .55  ⟦cite⟧ │
│ ● Missing      CCTV footage between 9–10 PM            (gap flagged by Risk)  │
```
`⟦cite⟧` → opens DocViewer at the source page/excerpt.

### 7b. Timeline tab
```
│  2024            TimelineRail (vertical)                                     │
│  │● 12 Jan  Incident — alleged assault           ⟦cite⟧                      │
│  │● 13 Jan  FIR No. 452 registered               ⟦cite⟧                      │
│  │● 02 Feb  Investigation / statements recorded  ⟦cite⟧                      │
│  │● 28 Mar  Charge sheet filed u/s 324 IPC       ⟦cite⟧                      │
│  │● 10 Jun  Trial commenced                        ⟦cite⟧                    │
```

### 7c. Issues tab
```
│ #1 PRIMARY   Whether the accused caused hurt with a dangerous weapon? ▰▰▰▰▱ │
│ #2 PRIMARY   Whether intention (mens rea) is established?             ▰▰▰▱▱ │
│ #3 SECONDARY Whether the FIR delay is fatal to prosecution?           ▰▰▱▱▱ │
```

### 7d. Statutes tab
```
│ Act / Provision                Category     Applicability         Conf      │
│ IPC §324 (hurt by weapon)      Criminal     Core charge           .90 ⟦cite⟧│
│ IPC §34 (common intention)     Criminal     Co-accused liability  .72 ⟦cite⟧│
│ CrPC §154 (FIR)                Criminal     Procedure / delay     .68 ⟦cite⟧│
```

### 7e. Arguments tab (ArgumentSplit)
```
│ ┌──── Petitioner ───────────────┐ ┌──── Respondent ──────────────────────┐ │
│ │ ▰▰▰▰ Self-defence; no intent  │ │ ▰▰▰▰ Eyewitness + medical evidence   │ │
│ │ ▰▰▰  FIR delay unexplained    │ │ ▰▰▰  Weapon recovered from accused   │ │
│ │ ▰▰   Contradictory witnesses  │ │ ▰▰   Motive established              │ │
│ └───────────────────────────────┘ └──────────────────────────────────────┘ │
```

### 7f. Evidence & 7g. Witnesses tabs
```
EVIDENCE                                    WITNESSES
● Documentary  Medical report   Strong  Hi   W1 Constable R.  ● reliable
● Electronic   (CCTV missing)   —       —      ↳ corroborates time of FIR
● Oral         Eyewitness PW1   Moderate Hi   W2 Neighbour S.  ⚠ contradiction
● Expert       Forensic (weapon)Strong  Hi      ↳ scene time differs from PW1
[Gaps: no CCTV; no recovery memo]            [Reliability + contradiction notes]
```

### 7h. Precedents tab
```
│ Rank  Case                         Citation        Relevance  Relationship  │
│  1    Bhagwan Singh v. State        (1976) 1 SCC    ▰▰▰▰▰ .91  Similar       │
│  2    State of UP v. Ramesh         (1989) 3 SCC    ▰▰▰▰  .78  Followed      │
│  3    K. Narayan v. State           (2003) 5 SCC    ▰▰▰   .64  Distinguished │
```

---

## 8. IRAC Dashboard (`/app/cases/:id/irac`)

```
┌────────────────────────────────────────────────────────────────────────────┐
│  IRAC — Sharma v. State            Issue ▾ (overall | #1 | #2 …)  [⤓ Export] │
├────────────────────────────────────────────────────────────────────────────┤
│ ┌ ISSUE ───────────────────────────────────────────────────────────────────┐│
│ │ Whether the accused caused hurt with a dangerous weapon u/s 324 IPC?      ││
│ ├ RULE ─────────────────────────────────────────────────────────────────────┤│
│ │ IPC §324; ingredients: voluntary hurt + dangerous weapon. ⟦cite⟧          ││
│ ├ APPLICATION ──────────────────────────────────────────────────────────────┤│
│ │ Medical report + recovery support hurt by weapon; defence pleads self-…   ││
│ ├ CONCLUSION ───────────────────────────────────────────────────────────────┤│
│ │ Prosecution likely to establish §324 if eyewitness survives cross. ⚠      ││
│ └───────────────────────────────────────────────────────────────────────────┘│
│  ⚠ Educational analysis — not legal advice.                                  │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 9. Legal Research Assistant — RAG Chat (`/app/cases/:id/chat`)

```
┌──────────────────────────────────────────────┬─────────────────────────────┐
│  Chat · Sharma v. State                       │  Sources                    │
│  ┌────────────────────────────────────────┐   │  ⟦1⟧ judgment.pdf p.4       │
│  │ You: What did PW1 say about the time?   │   │  ⟦2⟧ fir.pdf p.1            │
│  │ AI: PW1 stated the incident occurred ~  │   │  (click → DocViewer)        │
│  │     9:30 PM ⟦1⟧, but the FIR records ~  │   │                             │
│  │     11 PM ⟦2⟧ — a possible contradiction│   │  Confidence: ● High         │
│  │     [● High confidence]                 │   │                             │
│  └────────────────────────────────────────┘   │                             │
│  Ask about this case… ___________________ [↩] │  ⚠ Answers grounded in your │
│                                               │     documents only.         │
└──────────────────────────────────────────────┴─────────────────────────────┘
```

---

## 10. Strategy & Hearing Prep (advocate)

```
STRATEGY (/strategy)                          HEARING PREP (/hearing)
┌── Case Strength  ▰▰▰▱  62% ──────┐          ☐ Confirm bundle / paper-book
│ Strong: medical, recovery        │          ☐ Mark exhibits (12)
│ Weak:   FIR delay, PW2 contra.   │          ☐ Witness order: PW1, PW4
│ Missing: CCTV, recovery memo     │          ☐ Anticipated bench questions:
├── Judge Perspective ─────────────┤             • Explain FIR delay?
│ • Will probe delay in FIR        │             • Weapon chain of custody?
│ • Chain of custody of weapon     │          ☐ Key authorities: Bhagwan Singh
│ • Reliability of sole eyewitness │          ──────────────────────────────
├── Risk ──────────────────────────┤          Cross-exam assistant ▸
│ ● Evidentiary HIGH (CCTV gap)    │           PW2: highlight time contradiction
│ ● Procedural  MED  (FIR delay)   │           [⤓ Export hearing notes]
└──────────────────────────────────┘
```

---

## 11. Analytics Center (`/app/analytics`) — portfolio

```
┌────────────────────────────────────────────────────────────────────────────┐
│  Analytics Center            Range ▾  Court ▾  Type ▾            [⤓ Export]  │
├───────────────┬───────────────┬───────────────┬────────────────────────────┤
│ Avg Readiness │ High-risk cases│ Win-pattern*  │ Cases by stage             │
│   63% ▢       │    9 ●         │   —           │  ▢ (stacked bar)           │
├───────────────┴───────────────┴───────────────┴────────────────────────────┤
│  Readiness by matter ▢ (bars)      │  Risk by type ▢ (donut)                │
│  Subject-matter trends ▢ (lines)   │  Court patterns ▢                      │
└────────────────────────────────────────────────────────────────────────────┘  *trends are descriptive, not predictive
```

---

## 12. Admin — Users / AI Monitoring / Audit (`/app/admin/*`)

```
USERS                                   AI MONITORING
⌕ search  Role▾ Status▾  [+ Invite]     ┌ Tokens (24h) ▢  Cost $ ▢  p95 ms ▢ ┐
Name        Role      Status  Last      │ Agent error rate  1.4% ●           │
R. Sharma   Advocate  Active  2h        ├ Recent runs ─────────────────────── │
A. Khan     Student   Active  1d        │ run#... COMPLETED  facts✓ issues✓ … │
M. Verma    Firm Adm  Susp ●  5d        │ run#... PARTIAL    risk✗ (timeout)  │
[edit ▾ suspend]                        └─────────────────────────────────────┘

AUDIT LOGS                              DOCUMENT MONITORING
⌕ actor / action / resource  Range▾      Queue 3 · Processing 1 · Failed 2 ⚠
12:04 R.Sharma CASE_CREATED  case#…       fir_scan.jpg  FAILED (OCR) [retry]
12:05 R.Sharma DOC_UPLOADED  doc#…        deed.pdf      PROCESSING 40%
```

---

## 13. Settings (`/app/settings`)

```
┌──────────────┬─────────────────────────────────────────────────────────────┐
│ Profile      │  Profile                                                    │
│ Security     │  Name ____________  Email ____________  Role: Advocate       │
│ Appearance   │  Appearance:  Theme ( ☀ Light  ☾ Dark  ⚙ System )           │
│ Notifications│               Density ( Comfortable | Compact )              │
│ Team (firm)  │  Security:    Change password · Sessions · 2FA (v2)          │
│ Plan/Billing │                                          [ Save changes ]    │
└──────────────┴─────────────────────────────────────────────────────────────┘
```

---

## 14. Reports Center (`/app/reports`)

```
Generate / download exports:
 ▢ Case Analysis (full dashboard)  → PDF/DOCX     [Generate]
 ▢ Case Brief                      → PDF          [Generate]
 ▢ IRAC                            → PDF          [Generate]
 ▢ Hearing Notes                   → PDF          [Generate]
History:  Sharma_analysis.pdf  · 2h · 1.2MB  [download]
Footer on every export: "Generated by LexMind AI — analysis, not legal advice."
```

---

## 15. Cross-cutting States (apply everywhere)

```
LOADING        ▢▢▢ skeleton cards
EMPTY          "No cases yet."  [+ Upload your first case]  + sample cases
PROCESSING     Facts ✓ · Issues ◐ · Statutes ◌ · Arguments ◌   (progressive fill)
PARTIAL        "Risk agent failed — retry"  (other sections shown)
ERROR          "Couldn't load. [Retry]"      403 → "You don't have access."
```

---

_Previous: [← Information Architecture](02-information-architecture.md) · Next: [Component Hierarchy →](04-component-hierarchy.md)_
