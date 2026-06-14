# Installation Guide

## LexMind AI

**Version:** 1.0 · **Date:** 2026-06-14

> Fastest path is Docker Compose (one command). Full deployment options:
> [Phase 9 Deployment Guide](../phase-09-deployment/deployment.md).

---

## 1. Prerequisites
- **Docker** + **Docker Compose** (recommended path), OR
- For manual runs: **JDK 21**, **Node 20**, **Python 3.12**, **PostgreSQL 16**, **Qdrant**.
- ~8 GB RAM free for the full stack.

## 2. Get the code
```bash
git clone <your-repo-url> lexmind-ai
cd lexmind-ai
```

## 3. Option A — Full stack with Docker Compose (recommended)
```bash
cd infra
cp .env.example .env        # edit secrets for non-local use
docker compose up --build
```
Wait for all services to report healthy. Then open:

| Service | URL |
|---|---|
| **App** | http://localhost:8081 |
| Backend API / Swagger | http://localhost:8080/swagger-ui.html |
| AI service docs | http://localhost:8000/docs |
| Qdrant dashboard | http://localhost:6333/dashboard |

It runs with `LLM_PROVIDER=mock` (no API key). To use real Claude, set `LLM_PROVIDER=anthropic`
and `ANTHROPIC_API_KEY` in `infra/.env`, then `docker compose up --build`.

Stop: `docker compose down` (keep data) · `docker compose down -v` (wipe data).

## 4. Option B — Run services manually (development)

### 4.1 Start data services
```bash
cd infra && docker compose up postgres qdrant
```

### 4.2 Backend (JDK 21)
```bash
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/lexmind
export DB_USERNAME=lexmind DB_PASSWORD=lexmind
export JWT_SECRET="a-strong-256-bit-dev-secret-please-change"
export AI_SERVICE_URL=http://localhost:8000
mvn spring-boot:run        # http://localhost:8080  (Flyway migrates on start)
```

### 4.3 AI service (Python 3.12)
```bash
cd ai-service
python -m venv .venv && source .venv/Scripts/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload --port 8000               # http://localhost:8000/docs
```

### 4.4 Frontend (Node 20)
```bash
cd frontend
npm install
npm run dev                # http://localhost:5173  (proxies /api → :8080)
```

## 5. First-run smoke test
1. Open the app → **Sign up** (choose a role).
2. **Upload Case** → add a PDF/DOCX → **Create case** → **Analyze case**.
3. Watch the dashboard tabs fill; open **IRAC** and **Analytics**; try **chat**.

## 6. Running the tests
```bash
# AI service
cd ai-service && PYTHONPATH=. pytest --cov=app
# Frontend
cd frontend && npm run test
# Backend (needs Docker for Testcontainers)
cd backend && mvn verify
```

## 7. Common issues
| Issue | Resolution |
|---|---|
| Port already in use | Stop the conflicting process or change the published port in `infra/docker-compose.yml`. |
| Backend can't reach DB | Ensure Postgres is healthy (`docker compose ps`) and `DB_URL` is correct. |
| AI analysis fails | Confirm `ai-service` is up and `AI_SERVICE_TOKEN`/`INTERNAL_SERVICE_TOKEN` match. |
| Flyway validation error | Don't hand-edit the DB; let Flyway own the schema (`V1__init.sql`). |

## 8. Configuration reference
Per-service env: [`backend/.env.example`](../../backend/.env.example),
[`ai-service/.env.example`](../../ai-service/.env.example),
[`infra/.env.example`](../../infra/.env.example).
