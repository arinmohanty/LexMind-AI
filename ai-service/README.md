# LexMind AI — AI Service (FastAPI + LangGraph)

The AI tier: document pipeline (OCR/parse/chunk/embed → vector store), the **7-agent
analysis graph**, and **grounded RAG chat**. It produces the exact `AgentResultsPayload` the
backend ingests (see [docs/phase-02-architecture/05-ai-architecture.md](../docs/phase-02-architecture/05-ai-architecture.md)).

> Outputs are analytical aids, **not legal advice**.

## Runs offline out of the box
With `LLM_PROVIDER=mock` (default) there are **no API keys or heavy deps required** — a
deterministic mock provider drives the whole graph, RAG, and tests. Flip to real Claude +
Qdrant with config.

## Setup
```bash
cd ai-service
python -m venv .venv && . .venv/Scripts/activate   # (Windows: .venv\Scripts\activate)
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload --port 8000
#  docs: http://localhost:8000/docs   health: /health  /ready
```

## Enable real models / integrations
Uncomment the optional deps in `requirements.txt` and set env:
```
LLM_PROVIDER=anthropic
ANTHROPIC_API_KEY=sk-ant-...
LLM_MODEL_STRONG=claude-opus-4-8
QDRANT_URL=http://localhost:6333        # else in-memory store
# OCR/parsing: pip install pypdf python-docx pytesseract pillow
# Orchestration: pip install langgraph    (runner falls back to sequential if absent)
```

## Endpoints (all but health require the internal service token header)
```
GET  /health  /ready
POST /process    {documentId, caseId, text?}        → chunk+embed one document
POST /analyze    {caseId, documentIds[], documents?} → run 7-agent graph → AgentResultsPayload
POST /chat       {caseId, question, history[]}       → grounded answer + citations
```
`X-Internal-Token: <INTERNAL_SERVICE_TOKEN>` must match the backend's `AI_SERVICE_TOKEN`.

## Architecture
```
app/
  llm/         provider abstraction (mock | anthropic) + embeddings + factory
  agents/      state · prompts (versioned) · nodes (7 agents + IRAC) · graph (LangGraph) · runner
  pipeline/    extract (PDF/DOCX/OCR) · chunk · ingest · documents (backend fetch)
  vectorstore/ base · memory (fallback) · qdrant · factory
  rag/         grounded chat
  api/         analyze · process · chat · health
```
The graph: `Fact → Issue → {Statute, Argument} → {Precedent, Risk} → Judge → IRAC`. Each
agent records telemetry into `agentExecutions`; a single agent failure degrades the run to
`PARTIAL` rather than failing it.

## Test
```bash
python tests/test_graph_mock.py     # or: pytest
```
Verifies the graph emits a full, camelCase-correct payload and that RAG chat answers.
