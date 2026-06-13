# LexMind AI — Component Hierarchy & Frontend Structure

**Document:** Phase 3 / 04
**Status:** Draft for review
**Owner:** Frontend / UI Engineering
**Last updated:** 2026-06-14

> The React component tree, frontend folder structure, routing, and state strategy. This is
> the blueprint Phase 5 implements. Components map to the [Wireframes](03-wireframes.md) and
> consume the [Design System](01-design-system.md) tokens.

---

## 1. Frontend Folder Structure (Vite + React + TS)

Feature-first structure mirroring the backend bounded contexts.

```
frontend/
├── index.html
├── vite.config.ts
├── tailwind.config.ts          # references design-system CSS vars
├── tsconfig.json
├── .env.example                # VITE_API_BASE_URL=...
└── src/
    ├── main.tsx                # bootstraps providers
    ├── App.tsx                 # router + global providers
    ├── routes/                 # route tree + guards
    │   ├── index.tsx
    │   ├── ProtectedRoute.tsx
    │   └── RoleRoute.tsx
    ├── app/
    │   ├── providers/          # QueryClient, Theme, Auth, Toast
    │   ├── layout/             # AppShell, Sidebar, AppBar, Breadcrumbs, CommandMenu
    │   └── config/             # navConfig (role→nav), queryKeys, env
    ├── components/ui/          # ShadCN primitives (generated)
    ├── components/domain/      # LexMind domain components (see §4)
    ├── components/charts/      # Recharts wrappers
    ├── features/
    │   ├── auth/               # api, hooks, components, pages
    │   ├── cases/              # repository, create, case workspace shell
    │   ├── analysis/           # Case Analysis Dashboard + tabs
    │   ├── irac/
    │   ├── brief/
    │   ├── chat/               # RAG chat
    │   ├── documents/          # upload, DocViewer
    │   ├── evidence/  witnesses/  strategy/  hearing/
    │   ├── analytics/
    │   ├── research/           # citations, similar, trends, notes
    │   ├── admin/              # users, ai-monitoring, doc-monitoring, audit
    │   ├── reports/
    │   └── settings/
    ├── lib/
    │   ├── apiClient.ts        # axios/fetch + JWT interceptors + refresh
    │   ├── auth.ts             # token store, permission helpers
    │   ├── rbac.ts             # hasPermission(), can()
    │   └── utils.ts            # cn(), formatters, date utils
    ├── hooks/                  # useAuth, useTheme, useDebounce, useMediaQuery
    ├── types/                  # shared DTO types (generated from OpenAPI)
    └── pages/public/           # marketing pages (Home, About, Features, ...)
```

**Per-feature internal shape** (consistent):
```
features/<feature>/
├── api/        # typed API calls (thin)
├── hooks/      # React Query hooks (useCases, useCaseAnalysis, ...)
├── components/ # feature-scoped components
├── pages/      # route page components
└── types.ts
```

---

## 2. Provider & Routing Tree

```
<App>
└─ <QueryClientProvider>          React Query (server state)
   └─ <ThemeProvider>             light/dark/system
      └─ <AuthProvider>           JWT, current user, permissions
         └─ <TooltipProvider>
            └─ <Toaster/>         (Sonner)
            └─ <BrowserRouter>
               ├─ Public routes  (/, /login, /register, ...)
               └─ <ProtectedRoute>            (JWT required)
                  └─ <AppShell>               (sidebar + app bar + <Outlet/>)
                     ├─ /app/dashboard        → role-switched dashboard
                     ├─ /app/cases ...        → cases feature
                     ├─ /app/cases/:id/*      → <CaseWorkspace> (nested tabs)
                     ├─ /app/research/*
                     ├─ /app/analytics
                     ├─ /app/reports
                     ├─ /app/settings/*
                     └─ <RoleRoute role=...>  /app/admin/*
```

`<ProtectedRoute>` redirects unauthenticated → `/login?next=`. `<RoleRoute>` /
`requiredPermission` enforce RBAC client-side (server is the real gate).

---

## 3. App Shell Component Tree

```
<AppShell>
├─ <Sidebar>
│  ├─ <BrandMark/>
│  ├─ <NavList items={navConfig[role]}>   ← role-filtered (config/navConfig.ts)
│  │   └─ <NavItem icon label to badge?/>
│  └─ <QuickActionButton "+ Upload"/>
├─ <AppBar>
│  ├─ <Breadcrumbs/>
│  ├─ <CommandMenu trigger=⌘K/>           ← global search (cases, docs, actions)
│  ├─ <ThemeToggle/>
│  ├─ <NotificationsBell/>
│  └─ <ProfileMenu/>
└─ <main><Outlet/></main>
```

---

## 4. Case Analysis Dashboard — Component Tree (flagship screen)

```
<CaseWorkspace caseId>                         // shell: header + sub-tabs + <Outlet/>
├─ <CaseHeader>  (title, meta, [Export][Re-run], <AiDisclaimer/>)
├─ <CaseTabBar>  (Overview · Timeline · Facts · Issues · Statutes · Arguments ·
│                 Evidence · Witnesses · Precedents · IRAC · Brief · Chat · …)
└─ <AnalysisDashboard>
   ├─ <AnalysisStatusBanner/>        // partial/processing/last-run
   ├─ tab=overview → <OverviewTab>
   │     ├─ <CaseSnapshotCard/>      ├─ <PartiesCard/>
   │     ├─ <QuickStatsCard/>        └─ <MiniTimeline/>
   ├─ tab=timeline → <TimelineTab> → <TimelineRail><TimelineEvent .../></>
   ├─ tab=facts → <FactsTab>
   │     ├─ <FactStatusTabs/>        // Established/Disputed/Missing counts
   │     └─ <FactList> → <FactRow><FactStatusPill/><ConfidenceBadge/><CitationChip/></>
   ├─ tab=issues → <IssuesTab> → <IssueCard rank type score/>
   ├─ tab=statutes → <StatutesTab> → <StatuteTable/> (rows w/ <CitationChip/>)
   ├─ tab=arguments → <ArgumentsTab> → <ArgumentSplit>
   │     ├─ <ArgumentColumn side=PETITIONER> → <ArgumentItem><StrengthMeter/></>
   │     └─ <ArgumentColumn side=RESPONDENT>
   ├─ tab=evidence → <EvidenceTab> → <EvidenceCard type strength relevance/>
   ├─ tab=witnesses → <WitnessesTab> → <WitnessCard> → <StatementItem contradiction?/>
   └─ tab=precedents → <PrecedentsTab> → <PrecedentTable/> (relevance, relationship)

shared overlays:
├─ <DocViewerSheet/>     // opened by any <CitationChip/> → PDF at page/excerpt
└─ <ExportDialog/>
```

### Other flagship trees (abbreviated)
```
<IracPage>    → <IssueSelector/> + <IracCard issue rule application conclusion/> + <AiDisclaimer/>
<ChatPage>    → <ChatThread><ChatMessage citations/></> + <ChatComposer/> + <SourcesPanel/>
<StrategyPage>→ <CaseStrengthCard/> <JudgePerspectiveCard/> <RiskList/> <CrossExamPanel/>
<HearingPage> → <HearingChecklist/> <KeyAuthorities/> <BenchQuestions/> <ExportButton/>
<AnalyticsCenter> → <KpiRow/> + <ReadinessByMatterChart/> <RiskDonut/> <TrendLines/>
<AdminUsers>  → <UserTable/> <InviteDialog/> ; <AiMonitoring> → <UsageCharts/><RunsTable/>
<UploadWizard>→ <CaseMetaForm/> <UploadDropzone/> <FileStatusList/> <AnalyzeCTA/>
```

---

## 5. Domain Component Catalogue (props sketch)

| Component | Key props |
|---|---|
| `<CitationChip>` | `documentId, page, excerpt, chunkId` → opens `<DocViewerSheet>` |
| `<ConfidenceBadge>` | `value: 0..1` → Low/Med/High color |
| `<FactStatusPill>` | `status: ESTABLISHED\|DISPUTED\|MISSING` |
| `<StrengthMeter>` | `strength: STRONG\|MODERATE\|WEAK` |
| `<ReadinessGauge>` | `score: 0..1, label` (Recharts radial) |
| `<TimelineRail>` / `<TimelineEvent>` | `events[] / date, text, type, citation` |
| `<ArgumentSplit>` | `petitioner[], respondent[]` |
| `<IracCard>` | `issue, rule, application, conclusion` |
| `<UploadDropzone>` | `onFiles, accept, maxSize` |
| `<DocViewer>` | `storageKey, page, highlights[]` |
| `<AiDisclaimer>` | variant inline/banner |
| `<EmptyState>` / `<ProcessingState>` | `title, cta / agentProgress[]` |

All domain components are presentational + token-driven; data comes from React Query hooks.

---

## 6. State Management Strategy

| State kind | Tool | Examples |
|---|---|---|
| **Server state** | **React Query** (primary) | cases, analysis results, chat history, admin data — caching, polling for async analysis, optimistic updates |
| **Auth/session** | AuthProvider (context) + token store | current user, role, permissions, JWT refresh |
| **UI/global** | lightweight context / Zustand (only if needed) | theme, density, sidebar collapsed, command-menu open |
| **Form state** | React Hook Form + Zod | login, register, case create, settings |
| **URL state** | React Router params/search | active case, `?tab=`, filters, pagination |

**Async analysis pattern:** after `POST /analyze`, React Query polls `GET /analysis/:runId`
(or subscribes via SSE) with `refetchInterval` until `COMPLETED`, invalidating dashboard
section queries as they become available → progressive fill (matches wireframe ProcessingState).

**Query key convention** (`config/queryKeys.ts`):
```
['cases']                          ['cases', caseId]
['analysis', caseId, 'overview']   ['analysis', runId, 'status']
['irac', caseId]                   ['chat', caseId, sessionId]
['admin','users', filters]         ['analytics','portfolio', range]
```

---

## 7. API Integration Layer

- `lib/apiClient.ts`: single axios instance, `Authorization: Bearer` injected, **401 → silent
  refresh → retry**, error → normalized `ApiError` (matches backend envelope), trace id echoed.
- **Types generated from OpenAPI** (Phase 4 contract) into `src/types` → end-to-end type
  safety; no hand-drift between BE and FE DTOs.
- Each feature's `api/` wraps endpoints; hooks in `hooks/` wrap those in React Query.

---

## 8. RBAC in the UI

```ts
// lib/rbac.ts
can(user, 'evidence:analyze')        // boolean from JWT permissions
// usage
{can(user,'strategy:view') && <NavItem to="/app/.../strategy" />}
<RoleRoute requiredPermission="ai:monitor"> ...admin... </RoleRoute>
```
Server remains the authority; UI hiding is UX, not security.

---

## 9. Performance & Quality (Phase 5/8 hooks)

- **Code-split by route** (`React.lazy` + Suspense) — public site, app, and admin are
  separate chunks; heavy `<DocViewer>` and charts lazy-loaded.
- **Skeletons** for every async surface; **suspense boundaries** per dashboard tab.
- **Memoize** chart/data transforms; virtualize long tables (repository, audit).
- **a11y + visual tests** (axe, Storybook/Chromatic-style) and component tests (Vitest +
  Testing Library) defined in Phase 8.
- **Storybook** for the domain component catalogue (living design system).

---

## 10. Component Inventory Summary

| Layer | Count (approx) | Source |
|---|---|---|
| ShadCN primitives | ~25 | generated |
| Layout/shell | ~8 | AppShell, Sidebar, AppBar, CommandMenu, Breadcrumbs… |
| Domain components | ~20 | §5 catalogue |
| Chart wrappers | ~8 | Recharts |
| Feature pages | ~30 | one per route |
| Feature components | ~80 | tabs, cards, tables, dialogs |

This inventory feeds the Phase 5 build backlog.

---

_Previous: [← Wireframes](03-wireframes.md) · Phase 3 complete._
