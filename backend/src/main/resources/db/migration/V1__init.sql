-- =============================================================================
-- LexMind AI — PostgreSQL Schema (system of record)
-- Phase 2 / 04  ·  PostgreSQL 16+
-- This file is the canonical schema. In Phase 4 it becomes the first Flyway
-- migration (V1__init.sql). Embeddings live in Qdrant; this DB stores chunk
-- text + the Qdrant point id (document_chunks.qdrant_point_id).
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- fuzzy text search

-- ---------- shared trigger: maintain updated_at -----------------------------
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =============================================================================
-- 1. IDENTITY & ACCESS
-- =============================================================================

CREATE TABLE organizations (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(200) NOT NULL,
    type         VARCHAR(20)  NOT NULL DEFAULT 'SOLO'
                 CHECK (type IN ('SOLO','FIRM','INSTITUTION')),
    plan         VARCHAR(20)  NOT NULL DEFAULT 'FREE'
                 CHECK (plan IN ('FREE','STUDENT_PRO','ADVOCATE','FIRM','ENTERPRISE')),
    seat_limit   INT          NOT NULL DEFAULT 1 CHECK (seat_limit >= 1),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name  VARCHAR(30) NOT NULL UNIQUE
          CHECK (name IN ('SUPER_ADMIN','LAW_FIRM_ADMIN','ADVOCATE','RESEARCHER','LAW_STUDENT')),
    description VARCHAR(200)
);

CREATE TABLE permissions (
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code  VARCHAR(60) NOT NULL UNIQUE,   -- e.g. 'case:read'
    description VARCHAR(200)
);

CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id)       ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    scope         VARCHAR(10) NOT NULL DEFAULT 'OWN'
                  CHECK (scope IN ('ALL','FIRM','OWN','NONE')),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE','SUSPENDED','DELETED','PENDING')),
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_org ON users(organization_id);
CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);

CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =============================================================================
-- 2. CASE WORKSPACE
-- =============================================================================

CREATE TABLE cases (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id        UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    title           VARCHAR(300) NOT NULL,
    case_number     VARCHAR(100),
    court           VARCHAR(200),
    jurisdiction    VARCHAR(150),
    case_type       VARCHAR(40)
                    CHECK (case_type IN ('CRIMINAL','CIVIL','CONSTITUTIONAL','CONTRACT',
                          'FAMILY','LABOUR','CONSUMER','COMPANY','PROPERTY','TAX','OTHER')),
    stage           VARCHAR(40) DEFAULT 'INTAKE'
                    CHECK (stage IN ('INTAKE','INVESTIGATION','FILING','PLEADINGS',
                          'TRIAL','ARGUMENTS','JUDGMENT','APPEAL','CLOSED')),
    filing_date     DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE','ARCHIVED','DELETED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cases_owner_status ON cases(owner_id, status);
CREATE INDEX idx_cases_org_status   ON cases(organization_id, status);
CREATE INDEX idx_cases_number       ON cases(case_number);
CREATE INDEX idx_cases_title_trgm   ON cases USING gin (title gin_trgm_ops);
CREATE TRIGGER trg_cases_updated BEFORE UPDATE ON cases
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE case_parties (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id   UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    name      VARCHAR(200) NOT NULL,
    side      VARCHAR(20) NOT NULL
              CHECK (side IN ('PETITIONER','RESPONDENT','PLAINTIFF','DEFENDANT',
                              'APPELLANT','ACCUSED','COMPLAINANT','OTHER')),
    counsel   VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_parties_case ON case_parties(case_id);

CREATE TABLE case_members (
    case_id      UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_level VARCHAR(10) NOT NULL DEFAULT 'VIEW'
                 CHECK (access_level IN ('VIEW','EDIT','MANAGE')),
    added_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (case_id, user_id)
);

-- =============================================================================
-- 3. DOCUMENTS
-- =============================================================================

CREATE TABLE documents (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id       UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    uploaded_by   UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    original_filename VARCHAR(400) NOT NULL,
    storage_key   VARCHAR(500) NOT NULL,
    mime_type     VARCHAR(100) NOT NULL,
    size_bytes    BIGINT NOT NULL CHECK (size_bytes >= 0),
    doc_type      VARCHAR(30) DEFAULT 'UNKNOWN'
                  CHECK (doc_type IN ('FIR','CHARGE_SHEET','JUDGMENT','PETITION','AFFIDAVIT',
                        'CONTRACT','LEGAL_NOTICE','WRITTEN_STATEMENT','EVIDENCE',
                        'WITNESS_STATEMENT','UNKNOWN')),
    status        VARCHAR(20) NOT NULL DEFAULT 'QUEUED'
                  CHECK (status IN ('QUEUED','PROCESSING','DONE','FAILED')),
    page_count    INT DEFAULT 0,
    ocr_applied   BOOLEAN NOT NULL DEFAULT FALSE,
    checksum      VARCHAR(64),
    error_message TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_documents_case_status ON documents(case_id, status);
CREATE TRIGGER trg_documents_updated BEFORE UPDATE ON documents
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE document_chunks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id     UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    chunk_index     INT  NOT NULL,
    content         TEXT NOT NULL,
    token_count     INT,
    page_no         INT,
    qdrant_point_id UUID UNIQUE,    -- mirror of vector store point id
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (document_id, chunk_index)
);
CREATE INDEX idx_chunks_doc ON document_chunks(document_id, chunk_index);

-- =============================================================================
-- 4. ANALYSIS RUNS (versioned)
-- =============================================================================

CREATE TABLE analysis_runs (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id       UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    triggered_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'QUEUED'
                  CHECK (status IN ('QUEUED','PROCESSING','COMPLETED','FAILED','PARTIAL')),
    model         VARCHAR(80),
    total_tokens  BIGINT DEFAULT 0,
    cost_usd      NUMERIC(10,4) DEFAULT 0,
    error_message TEXT,
    started_at    TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_runs_case_status ON analysis_runs(case_id, status);

CREATE TABLE agent_executions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_run_id UUID NOT NULL REFERENCES analysis_runs(id) ON DELETE CASCADE,
    agent_type      VARCHAR(40) NOT NULL
                    CHECK (agent_type IN ('FACT_EXTRACTION','ISSUE_IDENTIFICATION',
                          'STATUTE_ANALYSIS','PRECEDENT_RESEARCH','ARGUMENT_BUILDER',
                          'RISK_ANALYSIS','JUDGE_PERSPECTIVE')),
    status          VARCHAR(20) NOT NULL DEFAULT 'QUEUED'
                    CHECK (status IN ('QUEUED','RUNNING','COMPLETED','FAILED')),
    latency_ms      INT,
    tokens          BIGINT DEFAULT 0,
    model           VARCHAR(80),
    output_json     JSONB,
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_agentexec_run ON agent_executions(analysis_run_id);

-- =============================================================================
-- 5. LEGAL INTELLIGENCE (results)
-- =============================================================================

CREATE TABLE case_facts (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id            UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    analysis_run_id    UUID REFERENCES analysis_runs(id) ON DELETE SET NULL,
    fact_text          TEXT NOT NULL,
    fact_status        VARCHAR(12) NOT NULL
                       CHECK (fact_status IN ('ESTABLISHED','DISPUTED','MISSING')),
    source_document_id UUID REFERENCES documents(id) ON DELETE SET NULL,
    source_excerpt     TEXT,
    confidence         NUMERIC(4,3) CHECK (confidence BETWEEN 0 AND 1),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_facts_case_status ON case_facts(case_id, fact_status);

CREATE TABLE timeline_events (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id            UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    event_date         DATE,
    event_text         TEXT NOT NULL,
    event_type         VARCHAR(40),   -- INCIDENT/FIR/INVESTIGATION/CHARGE_SHEET/HEARING/...
    sort_order         INT NOT NULL DEFAULT 0,
    source_document_id UUID REFERENCES documents(id) ON DELETE SET NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_timeline_case_order ON timeline_events(case_id, sort_order);

CREATE TABLE legal_issues (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id          UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    analysis_run_id  UUID REFERENCES analysis_runs(id) ON DELETE SET NULL,
    issue_text       TEXT NOT NULL,
    issue_type       VARCHAR(12) NOT NULL DEFAULT 'PRIMARY'
                     CHECK (issue_type IN ('PRIMARY','SECONDARY')),
    rank             INT NOT NULL DEFAULT 0,
    importance_score NUMERIC(4,3) CHECK (importance_score BETWEEN 0 AND 1),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_issues_case_rank ON legal_issues(case_id, rank);

-- Knowledge base of statutes/provisions (reference data)
CREATE TABLE statutes (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    act_name   VARCHAR(250) NOT NULL,
    section    VARCHAR(60),
    category   VARCHAR(40)
               CHECK (category IN ('CONSTITUTION','CRIMINAL','CIVIL','CONTRACT','FAMILY',
                     'LABOUR','CONSUMER','COMPANY','PROPERTY','TAX','OTHER')),
    description TEXT,
    UNIQUE (act_name, section)
);

CREATE TABLE case_statutes (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id            UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    statute_id         UUID NOT NULL REFERENCES statutes(id) ON DELETE RESTRICT,
    applicability_note TEXT,
    confidence         NUMERIC(4,3) CHECK (confidence BETWEEN 0 AND 1),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (case_id, statute_id)
);
CREATE INDEX idx_casestatutes_case    ON case_statutes(case_id);
CREATE INDEX idx_casestatutes_statute ON case_statutes(statute_id);

CREATE TABLE arguments (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id       UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    party_side    VARCHAR(12) NOT NULL
                  CHECK (party_side IN ('PETITIONER','RESPONDENT')),
    argument_text TEXT NOT NULL,
    strength      VARCHAR(10) CHECK (strength IN ('STRONG','MODERATE','WEAK')),
    source_excerpt TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_arguments_case_side ON arguments(case_id, party_side);

CREATE TABLE evidence_items (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id            UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    evidence_type      VARCHAR(15) NOT NULL
                       CHECK (evidence_type IN ('DOCUMENTARY','ORAL','ELECTRONIC','EXPERT')),
    description        TEXT NOT NULL,
    strength           VARCHAR(10) CHECK (strength IN ('STRONG','MODERATE','WEAK')),
    relevance          VARCHAR(10) CHECK (relevance IN ('HIGH','MEDIUM','LOW')),
    weakness_note      TEXT,
    source_document_id UUID REFERENCES documents(id) ON DELETE SET NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_evidence_case ON evidence_items(case_id);

CREATE TABLE witnesses (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id      UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    name         VARCHAR(200) NOT NULL,
    witness_type VARCHAR(20) CHECK (witness_type IN ('EYE','EXPERT','CHARACTER','HOSTILE','OTHER')),
    reliability_note TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_witnesses_case ON witnesses(case_id);

CREATE TABLE witness_statements (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    witness_id        UUID NOT NULL REFERENCES witnesses(id) ON DELETE CASCADE,
    statement_text    TEXT NOT NULL,
    contradiction_note TEXT,
    corroboration_note TEXT,
    source_document_id UUID REFERENCES documents(id) ON DELETE SET NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_wstmt_witness ON witness_statements(witness_id);

CREATE TABLE precedents (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id           UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    kb_judgment_id    UUID,   -- optional FK to corpus (set below after kb tables)
    cited_case_name   VARCHAR(400) NOT NULL,
    citation          VARCHAR(200),
    relevance_score   NUMERIC(4,3) CHECK (relevance_score BETWEEN 0 AND 1),
    relationship_type VARCHAR(20)
                      CHECK (relationship_type IN ('SIMILAR','LANDMARK','DISTINGUISHED','OVERRULED','FOLLOWED')),
    summary           TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_precedents_case ON precedents(case_id, relevance_score DESC);

CREATE TABLE irac_analyses (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id        UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    legal_issue_id UUID REFERENCES legal_issues(id) ON DELETE SET NULL,
    issue          TEXT NOT NULL,
    rule           TEXT NOT NULL,
    application    TEXT NOT NULL,
    conclusion     TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_irac_case ON irac_analyses(case_id);

CREATE TABLE case_briefs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id      UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    content_json JSONB NOT NULL,   -- {facts, issues, held, ratio, obiter, ...}
    generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_brief_case ON case_briefs(case_id);

-- =============================================================================
-- 6. ANALYTICS
-- =============================================================================

CREATE TABLE case_strength (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id         UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    analysis_run_id UUID REFERENCES analysis_runs(id) ON DELETE SET NULL,
    overall_score   NUMERIC(4,3) CHECK (overall_score BETWEEN 0 AND 1),
    findings_json   JSONB,  -- {strong[], weak[], missing_evidence[], open_questions[]}
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_strength_case ON case_strength(case_id);

CREATE TABLE risk_assessments (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id     UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    risk_type   VARCHAR(20) NOT NULL
                CHECK (risk_type IN ('PROCEDURAL','EVIDENTIARY','JURISDICTION','DOCUMENTATION')),
    severity    VARCHAR(10) CHECK (severity IN ('HIGH','MEDIUM','LOW')),
    description TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_risk_case ON risk_assessments(case_id);

CREATE TABLE readiness_scores (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id             UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    evidence_readiness  NUMERIC(4,3) CHECK (evidence_readiness BETWEEN 0 AND 1),
    witness_readiness   NUMERIC(4,3) CHECK (witness_readiness BETWEEN 0 AND 1),
    research_readiness  NUMERIC(4,3) CHECK (research_readiness BETWEEN 0 AND 1),
    hearing_readiness   NUMERIC(4,3) CHECK (hearing_readiness BETWEEN 0 AND 1),
    overall_readiness   NUMERIC(4,3) CHECK (overall_readiness BETWEEN 0 AND 1),
    computed_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_readiness_case ON readiness_scores(case_id);

-- =============================================================================
-- 7. CHAT / RESEARCH
-- =============================================================================

CREATE TABLE chat_sessions (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id    UUID NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title      VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_chatsess_case ON chat_sessions(case_id);

CREATE TABLE chat_messages (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id     UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role           VARCHAR(10) NOT NULL CHECK (role IN ('USER','ASSISTANT','SYSTEM')),
    content        TEXT NOT NULL,
    citations_json JSONB,   -- [{documentId, chunkId, excerpt, page}]
    tokens         INT DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_chatmsg_session ON chat_messages(session_id, created_at);

CREATE TABLE research_notes (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    case_id    UUID REFERENCES cases(id) ON DELETE SET NULL,
    title      VARCHAR(250) NOT NULL,
    content    TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notes_user ON research_notes(user_id);
CREATE TRIGGER trg_notes_updated BEFORE UPDATE ON research_notes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =============================================================================
-- 8. OBSERVABILITY
-- =============================================================================

CREATE TABLE audit_logs (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    action        VARCHAR(80) NOT NULL,       -- e.g. CASE_CREATED, DOCUMENT_UPLOADED
    resource_type VARCHAR(40) NOT NULL,
    resource_id   UUID,
    ip_address    VARCHAR(45),                -- IPv4/IPv6 textual form
    user_agent    VARCHAR(400),
    metadata_json JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_actor_time ON audit_logs(actor_user_id, created_at DESC);
CREATE INDEX idx_audit_resource   ON audit_logs(resource_type, resource_id);

CREATE TABLE ai_usage_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_run_id UUID REFERENCES analysis_runs(id) ON DELETE SET NULL,
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    operation       VARCHAR(40) NOT NULL,    -- EMBED, AGENT, CHAT
    model           VARCHAR(80),
    prompt_tokens   INT DEFAULT 0,
    completion_tokens INT DEFAULT 0,
    latency_ms      INT,
    cost_usd        NUMERIC(10,5) DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_aiusage_time ON ai_usage_logs(created_at DESC);

-- =============================================================================
-- 9. KNOWLEDGE BASE (precedent corpus for similar-case discovery)
-- =============================================================================

CREATE TABLE kb_judgments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_name    VARCHAR(400) NOT NULL,
    citation     VARCHAR(200),
    court        VARCHAR(200),
    judgment_date DATE,
    category     VARCHAR(40),
    summary      TEXT,
    source_url   VARCHAR(500),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_kbjudg_name_trgm ON kb_judgments USING gin (case_name gin_trgm_ops);

CREATE TABLE kb_judgment_chunks (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kb_judgment_id  UUID NOT NULL REFERENCES kb_judgments(id) ON DELETE CASCADE,
    chunk_index     INT NOT NULL,
    content         TEXT NOT NULL,
    qdrant_point_id UUID UNIQUE,
    UNIQUE (kb_judgment_id, chunk_index)
);

-- deferred FK: precedents.kb_judgment_id -> kb_judgments
ALTER TABLE precedents
    ADD CONSTRAINT fk_precedents_kb
    FOREIGN KEY (kb_judgment_id) REFERENCES kb_judgments(id) ON DELETE SET NULL;

-- =============================================================================
-- 10. SEED DATA — roles, permissions, role_permissions (from PRD RBAC matrix)
-- =============================================================================

INSERT INTO roles (name, description) VALUES
    ('SUPER_ADMIN','Platform operator: full access + monitoring'),
    ('LAW_FIRM_ADMIN','Firm tenant admin: firm-scoped management + analytics'),
    ('ADVOCATE','Practising advocate: own matters, evidence, strategy'),
    ('RESEARCHER','Legal researcher: research + citation tools'),
    ('LAW_STUDENT','Student: learning, IRAC, briefs');

INSERT INTO permissions (code, description) VALUES
    ('case:create','Create cases'),
    ('case:read','Read cases'),
    ('case:update','Update cases'),
    ('case:delete','Delete cases'),
    ('document:upload','Upload documents'),
    ('analysis:run','Run AI analysis'),
    ('evidence:analyze','Analyze evidence'),
    ('witness:analyze','Analyze witnesses'),
    ('strategy:view','View strategy dashboards'),
    ('irac:view','View IRAC dashboards'),
    ('research:citation','Use citation/precedent research'),
    ('analytics:firm','View firm analytics'),
    ('user:manage','Manage users'),
    ('audit:read','Read audit logs'),
    ('ai:monitor','Monitor AI usage'),
    ('system:configure','Configure platform');

-- Helper: grant permission to role with a scope
CREATE OR REPLACE FUNCTION grant_perm(p_role TEXT, p_perm TEXT, p_scope TEXT)
RETURNS void AS $$
    INSERT INTO role_permissions (role_id, permission_id, scope)
    SELECT r.id, p.id, p_scope FROM roles r, permissions p
    WHERE r.name = p_role AND p.code = p_perm
    ON CONFLICT DO NOTHING;
$$ LANGUAGE sql;

-- SUPER_ADMIN: everything (ALL)
SELECT grant_perm('SUPER_ADMIN', code, 'ALL') FROM permissions;

-- LAW_FIRM_ADMIN: firm-scoped management + analytics + audit
SELECT grant_perm('LAW_FIRM_ADMIN', code, 'FIRM') FROM (VALUES
    ('case:create'),('case:read'),('case:update'),('case:delete'),
    ('document:upload'),('analysis:run'),('evidence:analyze'),('witness:analyze'),
    ('strategy:view'),('irac:view'),('research:citation'),('analytics:firm'),
    ('user:manage'),('audit:read')) AS t(code);

-- ADVOCATE: own matters + practice tools
SELECT grant_perm('ADVOCATE', code, 'OWN') FROM (VALUES
    ('case:create'),('case:read'),('case:update'),('case:delete'),
    ('document:upload'),('analysis:run'),('evidence:analyze'),('witness:analyze'),
    ('strategy:view'),('irac:view'),('research:citation')) AS t(code);

-- RESEARCHER: own cases + research/citation (full)
SELECT grant_perm('RESEARCHER', 'research:citation', 'ALL');
SELECT grant_perm('RESEARCHER', code, 'OWN') FROM (VALUES
    ('case:create'),('case:read'),('case:update'),('case:delete'),
    ('document:upload'),('analysis:run'),('irac:view')) AS t(code);

-- LAW_STUDENT: own cases + learning
SELECT grant_perm('LAW_STUDENT', code, 'OWN') FROM (VALUES
    ('case:create'),('case:read'),('case:update'),('case:delete'),
    ('document:upload'),('analysis:run'),('irac:view'),('research:citation')) AS t(code);

-- =============================================================================
-- End of schema
-- =============================================================================
