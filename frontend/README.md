# LexMind AI — Frontend (React + TypeScript + Vite)

The SPA: role-aware app shell, auth, case repository, upload wizard, and the **Case Analysis
Dashboard** wired to the Phase 4 API. Built from the Phase 3 design system + component
hierarchy.

## Stack
React 18 · TypeScript · Vite · TailwindCSS (ShadCN-style tokens) · React Query · React
Router · Recharts · sonner · lucide-react.

## Run
```bash
cd frontend
npm install
npm run dev          # http://localhost:5173  (proxies /api → http://localhost:8080)
```
Start the backend (Phase 4) first so API calls resolve. Configure a non-proxied API base via
`VITE_API_BASE_URL` (see `.env.example`).

```bash
npm run typecheck    # tsc --noEmit
npm run build        # type-check + production build
```

## Structure (`src/`)
```
app/        providers (Query/Theme/Auth), layout (AppShell/Sidebar/AppBar), config (nav, queryKeys)
components/  ui/ (ShadCN-style primitives), domain/ (CitationChip, FactStatusPill, …)
features/    auth · cases · analysis · dashboard   (each: api · hooks · pages/components)
lib/         apiClient (axios + JWT refresh), auth (token store), rbac, utils
pages/       public landing, 404, placeholders
types/       API DTO types (mirror backend)
routes/      ProtectedRoute
```

## What works end-to-end
- Register / login with **silent JWT refresh** on 401, role-aware sidebar.
- Create a case → upload documents (status polled) → **Analyze** → redirected to the
  dashboard which **polls the run and progressively fills** the tabs.
- Case Analysis Dashboard: Overview · Timeline · Facts (status filter) · Issues · Arguments
  (side-by-side) · IRAC — every AI surface shows the **"analysis, not legal advice"** banner.
- Dark / light theme, responsive shell.

## Design ↔ code
Design tokens live as CSS variables in [`src/index.css`](src/index.css) and are mapped in
[`tailwind.config.ts`](tailwind.config.ts) — one source of truth shared with the ShadCN
primitives (Phase 3 / 01-design-system).

## Scope notes
Research, Analytics Center, and Admin screens are routed placeholders (built in Phases 7+).
RAG chat, evidence/witness/precedent tabs follow once the Phase 6 AI service produces them.
