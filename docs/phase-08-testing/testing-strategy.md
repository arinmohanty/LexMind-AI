# LexMind AI — Testing Strategy

**Document:** Phase 8 / 01
**Status:** Implemented (initial suites)
**Owner:** QA Engineering
**Last updated:** 2026-06-14

> Test approach across the three services, what each tier verifies, how to run it, and the
> coverage achieved vs. the 80% target.

---

## 1. Test Pyramid

```
                 ▲  few, slow, high-confidence
      ┌──────────────────────┐
      │  E2E (manual / Phase 9)│   upload → analyze → dashboard (Compose)
      ├──────────────────────┤
      │  Integration / API     │   Testcontainers + MockMvc (backend);
      │                        │   FastAPI TestClient (AI)
      ├──────────────────────┤
      │  Unit                  │   services, security, utils, agents,
      │                        │   components, hooks
      └──────────────────────┘
                 ▼  many, fast, isolated
```

We optimize for a **wide unit base** + **focused integration tests** on the risky seams
(auth, RBAC isolation, the AI contract, RAG grounding).

---

## 2. Per-Service Suites

### 2.1 AI service (Python · pytest) — ✅ executed, **82% coverage**, 20 tests
| Suite | Verifies |
|---|---|
| `test_graph_mock.py` | Full 7-agent payload + camelCase contract parity with the backend; RAG answers; analytics (strength/risk/readiness) present and in range |
| `test_runner_partial.py` | **Resilience** — a single failing agent → `FAILED` execution, run still returns (PARTIAL) |
| `test_providers.py` | Mock LLM dispatch by agent marker; deterministic, L2-normalized embeddings |
| `test_pipeline.py` | Chunking (overlap, empty); plain-text extraction fallback |
| `test_vectorstore.py` | In-memory store is **case-scoped** (no cross-case leakage); delete-by-case |
| `test_rag.py` | Chat returns citations from indexed chunks; **ungrounded → Low confidence** |
| `test_api.py` | Routes via `TestClient`; **internal-token guard returns 401**; analyze/process/chat happy paths |

Run:
```bash
cd ai-service && pip install -r requirements.txt pytest pytest-cov
PYTHONPATH=. pytest --cov=app --cov-report=term-missing
```
Uncovered lines are the optional integrations (Qdrant, Anthropic, backend-fetch) which require
live services and are exercised in Phase 9.

### 2.2 Frontend (TypeScript · Vitest + Testing Library) — ✅ executed, 18 tests
| Suite | Verifies |
|---|---|
| `lib/utils.test.ts` | `cn` tailwind-merge, `getErrorMessage` shapes, date formatting |
| `lib/rbac.test.ts` | Permission map per role (admin all, advocate vs student vs researcher), null user |
| `components/domain/domain.test.tsx` | `FactStatusPill`, `ConfidenceBadge` (incl. null), `AiDisclaimer` ("not legal advice"), `RunStatusBadge` |

Run:
```bash
cd frontend && npm install && npm run test      # vitest run
npm run coverage                                # v8 coverage
```
Test files are excluded from the production `tsc` build (`tsconfig.json` `exclude`).

### 2.3 Backend (Java · JUnit 5 + Mockito + Testcontainers) — written; runs on JDK 21 + Docker
| Suite | Type | Verifies |
|---|---|---|
| `JwtServiceTest` | unit | Token round-trip preserves id/email/role + permission authorities; tampered token rejected |
| `CaseAccessServiceTest` | unit (Mockito) | Owner/firm/super-admin access matrix; deleted-case hidden; denial → `NotFoundException` |
| `AuthFlowIT` | integration | register → `/me`; wrong password → `401 INVALID_CREDENTIALS` |
| `CaseRbacIT` | integration | Unauthenticated → 401; **a user cannot read another user's case (404)** |

Run (needs Docker for Testcontainers):
```bash
cd backend && mvn verify        # unit (surefire) + IT (failsafe) + JaCoCo report
# coverage: target/site/jacoco/index.html
```

---

## 3. Security Testing

| OWASP area | Test / control |
|---|---|
| Broken access control | `CaseRbacIT` (cross-tenant 404), `CaseAccessServiceTest`, method-level `@PreAuthorize` |
| Auth failures | `AuthFlowIT` (401 on bad creds, uniform message → no user enumeration); JWT signature verify in `JwtServiceTest` |
| Injection | JPA parameterized queries; bean validation on DTOs |
| Sensitive data exposure | error envelope hides stack traces; refresh tokens + reset tokens stored **hashed** |
| SSRF/secrets | internal endpoints gated by service token (`test_api.py` 401 case) |

A deeper pass (rate-limit tests, dependency scanning, ZAP) is scheduled with Phase 9 CI.

---

## 4. Coverage Targets vs. Status

| Service | Target | Status |
|---|---|---|
| AI service | 80% | **82% (measured)** |
| Frontend | 80% on critical libs/components | core libs + domain components covered; pages grow toward target |
| Backend | 80% | unit + integration written; JaCoCo wired — **measured once run on JDK 21 + Docker** |

> **Honest note:** the backend suite is authored to standard Spring Boot test conventions but
> has not been executed in this environment (JDK 20, no Maven/Docker). `mvn verify` on a JDK 21
> + Docker host (or the Phase 9 CI container) compiles and runs it and emits the JaCoCo report.

---

## 5. CI Hook (Phase 9 preview)

GitHub Actions matrix: `ai-service` (pytest), `frontend` (vitest + build), `backend`
(`mvn verify` with the Docker service for Testcontainers). Coverage uploaded as artifacts;
the pipeline fails if any suite fails.
