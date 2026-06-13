# LexMind AI — Design System

**Document:** Phase 3 / 01
**Status:** Draft for review
**Owner:** UI/UX Design
**Last updated:** 2026-06-14

> The visual + interaction language. These tokens map 1:1 to Tailwind theme config and ShadCN
> CSS variables in Phase 5, so design and code never drift.

---

## 1. Design Principles

1. **Trust over flash.** This is legal software handling sensitive matters — calm, precise,
   serious. Premium, not playful.
2. **Information density done right.** Lawyers scan; surface structure (cards, tabs, tables)
   with generous-but-disciplined spacing, not cramped walls of text.
3. **Grounded by default.** Any AI output shows a **citation affordance** and a
   confidence/disclaimer cue — trust is a UI feature, not just a backend one.
4. **Dual-mode native.** Dark and light are first-class, equally polished.
5. **Accessible (WCAG 2.1 AA leaning).** Contrast, focus rings, keyboard paths, semantic
   roles, motion-reduce.
6. **Responsive, desktop-first-but-not-only.** Rich analyst surfaces on desktop; collapse
   gracefully to tablet/mobile browser (and future native apps reuse the same patterns).

**Brand feel:** "the quiet confidence of a senior chambers" — deep navy/indigo authority,
a refined gold/amber accent (justice/scales), legible serif for legal headings + clean sans
for UI.

---

## 2. Color Tokens

Semantic tokens (HSL) → consumed as CSS variables. Light and dark are defined together.

### Brand & accent
| Token | Light | Dark | Use |
|---|---|---|---|
| `--brand` (primary) | `222 47% 24%` (deep indigo) | `222 47% 60%` | primary actions, active nav |
| `--brand-fg` | `0 0% 100%` | `222 47% 10%` | text on brand |
| `--accent` (gold) | `38 92% 50%` | `38 92% 55%` | highlights, scales/justice motif, key CTAs |
| `--accent-fg` | `38 70% 12%` | `38 70% 10%` | text on accent |

### Surfaces
| Token | Light | Dark |
|---|---|---|
| `--background` | `210 40% 98%` | `222 47% 7%` |
| `--foreground` | `222 47% 11%` | `210 40% 96%` |
| `--card` | `0 0% 100%` | `222 40% 11%` |
| `--muted` | `210 40% 94%` | `222 30% 16%` |
| `--muted-foreground` | `215 16% 42%` | `215 20% 65%` |
| `--border` | `214 28% 88%` | `222 25% 20%` |
| `--input` | `214 28% 88%` | `222 25% 22%` |
| `--ring` | `222 47% 40%` | `222 47% 60%` |

### Semantic status (used across dashboards)
| Token | Color | Meaning in app |
|---|---|---|
| `--success` | `142 70% 38%` | established fact, strong evidence/argument, ready |
| `--warning` | `38 92% 50%` | disputed fact, moderate strength, partial readiness |
| `--danger` | `0 72% 48%` | missing fact, weak/contradiction, high risk |
| `--info` | `205 80% 45%` | neutral info, citations, precedents |

> **Legal-semantic color mapping** (consistent everywhere):
> Established/Strong/Ready → green · Disputed/Moderate → amber · Missing/Weak/High-risk → red ·
> Precedent/Info → blue · Primary issue → indigo.

---

## 3. Typography

| Role | Font | Notes |
|---|---|---|
| Display / legal headings | **"Source Serif 4" / Lora** (serif) | gravitas for case titles, section headers |
| UI / body | **Inter** (sans) | dense UI, tables, forms |
| Mono / citations | **JetBrains Mono** | citation refs, code, IDs |

**Scale (rem):** `xs .75 · sm .875 · base 1 · lg 1.125 · xl 1.25 · 2xl 1.5 · 3xl 1.875 ·
4xl 2.25 · 5xl 3`. Line-height 1.5 body / 1.2 headings. Weight 400/500/600/700.

---

## 4. Spacing, Radius, Elevation, Motion

- **Spacing scale (px):** 2,4,8,12,16,20,24,32,40,48,64 (Tailwind 0.5–16). Base rhythm = 4px.
- **Radius:** `sm 6px · md 8px · lg 12px · xl 16px · full`. Default card = `lg`.
- **Elevation:** 3 levels — `e1` subtle (cards), `e2` (popovers/dropdowns), `e3` (modals).
  Dark mode uses border + slight lightness instead of heavy shadows.
- **Motion:** 150–200ms ease-out for hover/expand; 250ms for route/page transitions;
  respect `prefers-reduced-motion`. No gratuitous animation.

---

## 5. Component Library (ShadCN UI baseline + LexMind specials)

**ShadCN primitives used:** Button, Input, Textarea, Select, Checkbox, Switch, Tabs, Card,
Dialog, Sheet, Dropdown, Popover, Tooltip, Toast/Sonner, Table, Badge, Avatar, Skeleton,
Accordion, Separator, Command (⌘K), Progress, ScrollArea, Calendar/DatePicker, Breadcrumb.

**LexMind domain components** (composed on top — full tree in
[04-component-hierarchy.md](04-component-hierarchy.md)):

| Component | Purpose |
|---|---|
| `<CitationChip>` | clickable source ref → opens PDF at page/excerpt |
| `<ConfidenceBadge>` | low/med/high confidence cue on AI output |
| `<FactStatusPill>` | Established / Disputed / Missing (green/amber/red) |
| `<StrengthMeter>` | Strong/Moderate/Weak bar for args & evidence |
| `<ReadinessGauge>` | radial gauge for readiness scores (Recharts) |
| `<TimelineRail>` | vertical legal chronology |
| `<ArgumentSplit>` | side-by-side petitioner/respondent panels |
| `<IracCard>` | Issue/Rule/Application/Conclusion block |
| `<UploadDropzone>` | drag-drop + OCR/processing status |
| `<DocViewer>` | in-app PDF viewer with highlight/annotation |
| `<AiDisclaimer>` | persistent "analysis, not legal advice" banner |
| `<EmptyState>` / `<ProcessingState>` | first-run + async-analysis states |

---

## 6. Data Visualization (Recharts)

| Chart | Where |
|---|---|
| Radial/gauge | readiness scores, case-strength |
| Horizontal bars | evidence/argument strength distribution, statute relevance |
| Network/graph | citation networks (custom/Recharts+d3) |
| Stacked bars / lines | legal trends, success rates, court patterns |
| Donut | fact-status mix (established/disputed/missing) |

Charts inherit semantic status colors; always paired with an accessible table fallback.

---

## 7. Layout System

- **App shell:** fixed left **sidebar** (role-aware nav) + top **app bar** (global ⌘K search,
  breadcrumbs, theme toggle, notifications, profile) + scrollable content.
- **Grid:** 12-col fluid; content max-width 1440; dashboard cards in 1–3 col responsive grid.
- **Breakpoints:** `sm 640 · md 768 · lg 1024 · xl 1280 · 2xl 1536`. Sidebar collapses to
  icon rail < lg, to a Sheet drawer < md.
- **Density toggle:** comfortable / compact (advocates with big repositories).

---

## 8. States & Feedback

Every async surface defines: **loading (Skeleton)**, **empty (EmptyState + CTA)**,
**processing (ProcessingState + progress)**, **error (retry)**, **success**. AI sections
additionally show **partial** (some agents done, others running) so dashboards fill in
progressively.

---

## 9. Accessibility Checklist (enforced in Phase 5/8)

- Contrast ≥ 4.5:1 text / 3:1 large & UI; verified in both themes.
- Visible focus ring (`--ring`) on every interactive element; full keyboard nav incl. ⌘K.
- Semantic landmarks (`nav/main/aside`), ARIA on custom widgets, labelled form fields + error
  text tied via `aria-describedby`.
- `prefers-reduced-motion` honored; no info conveyed by color alone (pair with icon/label).
- Target size ≥ 44px on touch.

---

## 10. Theming Implementation Note (for Phase 5)

Tokens live as CSS variables on `:root` (light) and `.dark`. Tailwind `theme.extend.colors`
references `hsl(var(--token))`. ShadCN components consume the same variables → one source of
truth, instant dark/light, and easy white-labeling for firms later.

---

_Next: [Information Architecture →](02-information-architecture.md)_
