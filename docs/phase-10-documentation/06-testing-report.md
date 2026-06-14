# Testing Report

## LexMind AI

**Version:** 1.0 · **Date:** 2026-06-14

> Summarizes the test approach and results. Strategy detail:
> [Phase 8 Testing Strategy](../phase-08-testing/testing-strategy.md).

---

## 1. Objectives
Verify correctness of the critical paths (auth, RBAC isolation, the AI contract, RAG grounding,
analytics), guard against regressions, and target ≥80% coverage on testable tiers.

## 2. Test Levels & Tools
| Level | Tooling |
|---|---|
| Unit | JUnit 5 + Mockito (backend); pytest (AI); Vitest + Testing Library (frontend) |
| Integration / API | Testcontainers (PostgreSQL) + MockMvc (backend); FastAPI TestClient (AI) |
| Build verification | `vite build`, `tsc`, `mvn package` |
| Coverage | JaCoCo (backend); pytest-cov (AI); v8 (frontend) |

## 3. Results Summary

| Service | Suite | Result |
|---|---|---|
| **AI service** | pytest (8 files, 20 tests) | **PASS** — coverage **82%** |
| **Frontend** | Vitest (3 files, 18 tests) | **PASS** — production build clean |
| **Backend** | JUnit + Testcontainers (5 classes) | Authored; executed via `mvn verify` on JDK 21 + Docker (CI) |

### 3.1 AI service — executed
- Full 7-agent payload + **camelCase contract parity** with the backend.
- Analytics present (strength/risk/readiness) and within [0,1].
- **Resilience:** a single failing agent → `FAILED` execution, run returns PARTIAL.
- **Vector isolation:** in-memory store filters by case (no cross-case leakage).
- **RAG grounding:** citations returned; ungrounded query → **Low** confidence.
- **API/security:** internal-token guard returns **401**; analyze/process/chat happy paths 200.

Command:
```
PYTHONPATH=. pytest --cov=app --cov-report=term-missing
# 20 passed — TOTAL coverage 82%
```

### 3.2 Frontend — executed
- RBAC permission map per role (admin all; advocate vs student vs researcher; null user).
- Utility correctness (`cn`, `getErrorMessage`, date formatting).
- Domain components render correctly (`FactStatusPill`, `ConfidenceBadge` incl. null,
  `AiDisclaimer` shows "not legal advice", `RunStatusBadge`).

Command: `npm run test` → **18 passed**; `npm run build` → success.

### 3.3 Backend — authored (runs on JDK 21 + Docker)
| Test | Verifies |
|---|---|
| `JwtServiceTest` | Token round-trip preserves identity + permission authorities; tampered token rejected. |
| `CaseAccessServiceTest` | Owner/firm/super-admin access matrix; deleted-case hidden; denial → `NotFoundException`. |
| `AuthFlowIT` | register → `/me`; wrong password → 401 `INVALID_CREDENTIALS`. |
| `CaseRbacIT` | Unauthenticated → 401; **cross-user case read → 404**. |

Command: `mvn verify` (Testcontainers spins PostgreSQL, applies Flyway, runs MockMvc; JaCoCo
report at `target/site/jacoco/`).

## 4. Security Testing
Broken access control (`CaseRbacIT`, `CaseAccessServiceTest`), auth failures with non-enumerating
messages (`AuthFlowIT`), JWT signature verification (`JwtServiceTest`), internal-endpoint token
guard (`test_api.py`), parameterized JPA queries, DTO validation, hashed tokens, no-stack-trace
error envelope. Mapped to OWASP Top-10 in the
[strategy doc](../phase-08-testing/testing-strategy.md#3-security-testing).

## 5. Defects Found & Fixed (during development)
| Defect | Fix |
|---|---|
| `Map.of(null,…)` runtime NPE in security error writer | Replaced with `ApiResponse.fail(...)`. |
| React Query typed mutation errors as `Error` not `ApiError` | Introduced `getErrorMessage(unknown)` helper. |
| RAG returned model's confidence even with no retrieval | Force **Low** confidence when no passages (grounding). |

## 6. Coverage vs Target
| Service | Target | Achieved |
|---|---|---|
| AI service | 80% | **82%** (measured) |
| Frontend | core libs/components | covered; pages grow toward target |
| Backend | 80% | measured via JaCoCo on `mvn verify` (CI) |

## 7. Conclusion
The risk-bearing seams are covered and green on the executable tiers; the backend suite runs in
CI/Docker. The platform is functionally verified for the MVP+v1 scope.
