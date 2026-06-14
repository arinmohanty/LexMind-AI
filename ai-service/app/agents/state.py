from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from datetime import date

from app.llm.base import LLMResult
from app.schemas import (
    AgentExecutionPayload,
    AgentResultsPayload,
    ArgumentPayload,
    CaseStrengthPayload,
    FactPayload,
    IracPayload,
    IssuePayload,
    ReadinessPayload,
    RiskPayload,
    TimelinePayload,
)

CONTEXT_CHAR_BUDGET = 16_000


@dataclass
class AgentState:
    """Shared, accumulating state for the analysis graph (AI architecture §4)."""

    case_id: str
    context: str
    facts: list[FactPayload] = field(default_factory=list)
    timeline: list[TimelinePayload] = field(default_factory=list)
    issues: list[IssuePayload] = field(default_factory=list)
    arguments: list[ArgumentPayload] = field(default_factory=list)
    irac: list[IracPayload] = field(default_factory=list)
    risks: list[RiskPayload] = field(default_factory=list)
    case_strength: CaseStrengthPayload | None = None
    readiness: ReadinessPayload | None = None
    agent_executions: list[AgentExecutionPayload] = field(default_factory=list)
    total_tokens: int = 0
    cost_usd: float = 0.0
    model: str = ""

    def record_usage(self, result: LLMResult) -> None:
        self.total_tokens += result.total_tokens
        self.cost_usd += estimate_cost(result)
        self.model = result.model

    def to_payload(self) -> AgentResultsPayload:
        return AgentResultsPayload(
            model=self.model or None,
            total_tokens=self.total_tokens,
            cost_usd=round(self.cost_usd, 6),
            facts=self.facts,
            timeline=self.timeline,
            issues=self.issues,
            arguments=self.arguments,
            irac=self.irac,
            case_strength=self.case_strength,
            risks=self.risks,
            readiness=self.readiness,
            agent_executions=self.agent_executions,
        )


# Per-million-token blended rates (USD). Mock is free.
_RATES = {"input": 3.0, "output": 15.0}


def estimate_cost(result: LLMResult) -> float:
    if not result.model or result.model.startswith("mock"):
        return 0.0
    return (result.input_tokens * _RATES["input"] + result.output_tokens * _RATES["output"]) / 1_000_000


def build_context(documents: list[tuple[str | None, str]]) -> str:
    """Join (filename, text) docs into a single bounded context block."""
    parts: list[str] = []
    for filename, text in documents:
        header = f"--- DOCUMENT: {filename or 'document'} ---"
        parts.append(f"{header}\n{text.strip()}")
    joined = "\n\n".join(parts)
    return joined[:CONTEXT_CHAR_BUDGET]


def parse_json(text: str) -> dict:
    """Best-effort JSON extraction from an LLM response (handles prose wrappers/fences)."""
    if not text:
        return {}
    fenced = re.search(r"```(?:json)?\s*(\{.*?\})\s*```", text, re.DOTALL)
    candidate = fenced.group(1) if fenced else None
    if candidate is None:
        start = text.find("{")
        end = text.rfind("}")
        candidate = text[start : end + 1] if start != -1 and end != -1 else text
    try:
        return json.loads(candidate)
    except (json.JSONDecodeError, ValueError):
        return {}


def parse_date(value: str | None) -> date | None:
    if not value:
        return None
    try:
        return date.fromisoformat(value[:10])
    except ValueError:
        return None
