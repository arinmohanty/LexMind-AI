# LexMind AI — Deployment Guide

**Document:** Phase 9 / 01
**Status:** Implemented
**Owner:** DevOps
**Last updated:** 2026-06-14

How to run the whole platform locally with one command, and how to deploy to Railway,
Render, or AWS. CI is in [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml).

---

## 1. Architecture recap (runtime)

```
[ browser ] → frontend (nginx :8081)
                 │  /api/* proxied
                 ▼
              backend (Spring Boot :8080) ──→ PostgreSQL
                 │  REST + X-Internal-Token
                 ▼
              ai-service (FastAPI :8000) ──→ Qdrant
                 └──→ LLM provider (mock | Claude)
```

The frontend proxies `/api` to the backend (same-origin, no CORS). The backend calls the AI
service with the internal service token; the AI service fetches document bytes back from the
backend's `/internal` endpoint and stores vectors in Qdrant.

---

## 2. Local — full stack with Docker Compose

**Prereqs:** Docker + Docker Compose.

```bash
cd infra
cp .env.example .env          # edit secrets for anything non-local
docker compose up --build
```

| Service | URL |
|---|---|
| Frontend (app) | http://localhost:8081 |
| Backend API | http://localhost:8080/api/v1 · Swagger: http://localhost:8080/swagger-ui.html |
| AI service | http://localhost:8000/docs |
| Qdrant | http://localhost:6333/dashboard |

**First run:** open the app → register → create a case → upload a document → **Analyze**.
With `LLM_PROVIDER=mock` (default) the 7 agents return structured analysis with **no API key**.
Set `LLM_PROVIDER=anthropic` + `ANTHROPIC_API_KEY` in `.env` for real Claude analysis.

Tear down (keep data): `docker compose down` · wipe data: `docker compose down -v`.

> The **backend image build compiles the project on JDK 21** (`maven:3.9-eclipse-temurin-21`
> stage) — so a successful `docker compose build` is also the backend's compile gate.

---

## 3. Local — run services individually (dev)

| Service | Command |
|---|---|
| Postgres + Qdrant | `docker compose up postgres qdrant` |
| Backend | `cd backend && mvn spring-boot:run` (JDK 21) |
| AI service | `cd ai-service && pip install -r requirements.txt && uvicorn app.main:app --reload --port 8000` |
| Frontend | `cd frontend && npm install && npm run dev` (Vite proxies `/api` → :8080) |

---

## 4. Environment variables (key)

| Var | Service | Notes |
|---|---|---|
| `JWT_SECRET` | backend | **must** be a strong 256-bit secret in prod |
| `INTERNAL_SERVICE_TOKEN` / `AI_SERVICE_TOKEN` | both | must match between backend ↔ ai-service |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | backend | PostgreSQL connection |
| `AI_SERVICE_URL` | backend | e.g. `http://ai-service:8000` |
| `LLM_PROVIDER` / `ANTHROPIC_API_KEY` | ai-service | `mock` or `anthropic` |
| `QDRANT_URL` | ai-service | empty → in-memory (dev only) |
| `CORS_ORIGINS` | backend | frontend origin if not same-origin |

Full lists: [`backend/.env.example`](../../backend/.env.example),
[`ai-service/.env.example`](../../ai-service/.env.example),
[`infra/.env.example`](../../infra/.env.example).

---

## 5. Cloud deployment

### 5.1 Railway / Render (managed PaaS — easiest)
Each service deploys from its folder using its Dockerfile.

1. **PostgreSQL** — provision the managed Postgres add-on; copy its connection string into the
   backend's `DB_URL/DB_USERNAME/DB_PASSWORD`.
2. **Qdrant** — use Qdrant Cloud (set `QDRANT_URL` + `QDRANT_API_KEY`) or deploy the
   `qdrant/qdrant` image as a service.
3. **ai-service** — deploy `ai-service/` (Dockerfile); set `LLM_PROVIDER`, `ANTHROPIC_API_KEY`,
   `QDRANT_URL`, `INTERNAL_SERVICE_TOKEN`, `BACKEND_BASE_URL`.
4. **backend** — deploy `backend/`; set DB vars, `JWT_SECRET`, `AI_SERVICE_URL`,
   `AI_SERVICE_TOKEN`, `CORS_ORIGINS` (the frontend's public URL).
5. **frontend** — deploy `frontend/`; point the nginx `proxy_pass` (or `VITE_API_BASE_URL`) at
   the backend's public URL.

Render: a `render.yaml` blueprint can codify the above (4 web services + managed Postgres).

### 5.2 AWS (more control)
- **Images:** push to ECR (`docker build` + `docker push`).
- **Compute:** ECS Fargate services (one task def per service) behind an ALB, *or* EKS.
- **Data:** RDS PostgreSQL; Qdrant on a container/EC2 (or Qdrant Cloud).
- **Storage:** switch document storage to S3 (`STORAGE_BACKEND=s3` — the `StorageService` S3
  impl is the Phase-9+ extension point; `LocalStorageService` is dev/default).
- **Secrets:** AWS Secrets Manager / SSM for `JWT_SECRET`, `ANTHROPIC_API_KEY`, DB creds.
- **TLS:** ACM cert on the ALB; HTTPS only.

---

## 6. CI/CD

[`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) runs on every push/PR:

| Job | Does |
|---|---|
| `ai-service` | `pytest --cov` (uploads coverage.xml) |
| `frontend` | `npm run test` + `npm run build` |
| `backend` | `mvn verify` — **compiles on JDK 21** + runs unit + Testcontainers integration tests + JaCoCo |
| `docker-build` | builds all three images (gate; add push to ECR/registry for CD) |

To enable continuous **deployment**, add a deploy job after `docker-build` that pushes images
and triggers the platform (Railway/Render API, or `aws ecs update-service`).

---

## 7. Production hardening checklist

- [ ] Replace all default secrets (`JWT_SECRET`, `INTERNAL_SERVICE_TOKEN`, DB password).
- [ ] TLS everywhere; HSTS at the edge.
- [ ] Managed Postgres with automated backups + PITR.
- [ ] Qdrant Cloud or persistent, backed-up Qdrant volume.
- [ ] S3 (or compatible) object storage with encryption at rest for documents.
- [ ] Rate limiting + WAF at the edge.
- [ ] Centralized logs + metrics (the services expose health/readiness + AI usage metrics).
- [ ] Set `LLM_PROVIDER=anthropic` (or a self-hosted model for sensitive tenants) and a cost ceiling.
- [ ] Run `docker compose build` / CI green before promoting.
