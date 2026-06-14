"""
Wire contract with the backend (mirrors AiContracts.java). All models serialize to
camelCase so Jackson on the backend binds them directly. FastAPI emits aliases by default.
"""
from __future__ import annotations

from datetime import date

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True)


# ---------- Analyze request ----------

class InlineDocument(CamelModel):
    id: str | None = None
    filename: str | None = None
    text: str


class RunAgentsRequest(CamelModel):
    case_id: str
    document_ids: list[str] = Field(default_factory=list)
    options: dict = Field(default_factory=dict)
    # Optional: documents supplied inline (tests / when the caller already has text).
    documents: list[InlineDocument] | None = None


# ---------- Structured agent outputs (== backend AgentResultsPayload) ----------

class FactPayload(CamelModel):
    fact_text: str
    fact_status: str = "ESTABLISHED"          # ESTABLISHED | DISPUTED | MISSING
    source_excerpt: str | None = None
    confidence: float | None = None


class TimelinePayload(CamelModel):
    event_date: date | None = None
    event_text: str
    event_type: str | None = None
    sort_order: int | None = None


class IssuePayload(CamelModel):
    issue_text: str
    issue_type: str = "PRIMARY"               # PRIMARY | SECONDARY
    rank: int | None = None
    importance_score: float | None = None


class ArgumentPayload(CamelModel):
    party_side: str                           # PETITIONER | RESPONDENT
    argument_text: str
    strength: str | None = None               # STRONG | MODERATE | WEAK
    source_excerpt: str | None = None


class IracPayload(CamelModel):
    issue: str
    rule: str
    application: str
    conclusion: str


class AgentExecutionPayload(CamelModel):
    agent_type: str
    status: str = "COMPLETED"                 # COMPLETED | FAILED
    latency_ms: int | None = None
    tokens: int | None = None
    model: str | None = None
    output_json: str | None = None
    error_message: str | None = None


class AgentResultsPayload(CamelModel):
    model: str | None = None
    total_tokens: int | None = 0
    cost_usd: float | None = 0.0
    facts: list[FactPayload] = Field(default_factory=list)
    timeline: list[TimelinePayload] = Field(default_factory=list)
    issues: list[IssuePayload] = Field(default_factory=list)
    arguments: list[ArgumentPayload] = Field(default_factory=list)
    irac: list[IracPayload] = Field(default_factory=list)
    agent_executions: list[AgentExecutionPayload] = Field(default_factory=list)


# ---------- RAG chat ----------

class ChatTurn(CamelModel):
    role: str
    content: str


class ChatRequest(CamelModel):
    case_id: str
    question: str
    history: list[ChatTurn] = Field(default_factory=list)


class Citation(CamelModel):
    document_id: str | None = None
    chunk_id: str | None = None
    excerpt: str | None = None
    page: int | None = None


class ChatAnswer(CamelModel):
    answer: str
    citations: list[Citation] = Field(default_factory=list)
    confidence: str | None = None             # High | Medium | Low


# ---------- Document processing ----------

class ProcessRequest(CamelModel):
    document_id: str
    case_id: str
    text: str | None = None                   # if provided, skip extraction


class ProcessResponse(CamelModel):
    document_id: str
    chunks: int
    pages: int | None = None
    ocr_applied: bool = False
    doc_type: str | None = None
