"""
The 7 agents + IRAC composer as plain functions over AgentState. Each is wrapped with
telemetry + error capture so a single agent failure degrades to PARTIAL rather than failing
the whole run (LLD §7 resilience). Used by both the sequential runner and the LangGraph graph.
"""
from __future__ import annotations

import json
import time

from app.agents import prompts as P
from app.agents.state import AgentState, parse_date, parse_json
from app.llm.base import LLMProvider
from app.schemas import (
    AgentExecutionPayload,
    ArgumentPayload,
    CaseStrengthPayload,
    FactPayload,
    IracPayload,
    IssuePayload,
    ReadinessPayload,
    RiskPayload,
    TimelinePayload,
)


def _clamp(value: float) -> float:
    return round(max(0.0, min(1.0, value)), 3)


def _execute(state: AgentState, llm: LLMProvider, agent_type: str, system: str, user: str) -> dict:
    """Run one agent: call the LLM, record telemetry, return parsed JSON (or {} on failure)."""
    start = time.perf_counter()
    try:
        result = llm.complete(system, user)
        state.record_usage(result)
        data = parse_json(result.text)
        state.agent_executions.append(AgentExecutionPayload(
            agent_type=agent_type,
            status="COMPLETED",
            latency_ms=int((time.perf_counter() - start) * 1000),
            tokens=result.total_tokens,
            model=result.model,
            output_json=json.dumps(data),
        ))
        return data
    except Exception as exc:  # noqa: BLE001 - capture and continue (partial tolerance)
        state.agent_executions.append(AgentExecutionPayload(
            agent_type=agent_type,
            status="FAILED",
            latency_ms=int((time.perf_counter() - start) * 1000),
            error_message=str(exc),
        ))
        return {}


# ---- Agent 1: Fact Extraction (facts + timeline) ----
def fact_extraction(state: AgentState, llm: LLMProvider) -> None:
    data = _execute(state, llm, "FACT_EXTRACTION", P.FACT_SYSTEM,
                    P.user_with_context("Extract facts, dates, parties and events.", state.context))
    for f in data.get("facts", []):
        state.facts.append(FactPayload(
            fact_text=f.get("factText", ""),
            fact_status=(f.get("factStatus") or "ESTABLISHED").upper(),
            source_excerpt=f.get("sourceExcerpt"),
            confidence=f.get("confidence"),
        ))
    for i, t in enumerate(data.get("timeline", [])):
        state.timeline.append(TimelinePayload(
            event_date=parse_date(t.get("eventDate")),
            event_text=t.get("eventText", ""),
            event_type=t.get("eventType"),
            sort_order=i,
        ))


# ---- Agent 2: Issue Identification ----
def issue_identification(state: AgentState, llm: LLMProvider) -> None:
    data = _execute(state, llm, "ISSUE_IDENTIFICATION", P.ISSUE_SYSTEM,
                    P.user_with_context("Identify and rank the legal issues.", state.context))
    for idx, it in enumerate(data.get("issues", []), start=1):
        state.issues.append(IssuePayload(
            issue_text=it.get("issueText", ""),
            issue_type=(it.get("issueType") or "PRIMARY").upper(),
            rank=idx,
            importance_score=it.get("importanceScore"),
        ))


# ---- Agent 3: Statute Analysis (telemetry-only; surfaced via outputJson) ----
def statute_analysis(state: AgentState, llm: LLMProvider) -> None:
    _execute(state, llm, "STATUTE_ANALYSIS", P.STATUTE_SYSTEM,
             P.user_with_context("Map applicable statutes and provisions.", state.context))


# ---- Agent 4: Precedent Research (telemetry-only) ----
def precedent_research(state: AgentState, llm: LLMProvider) -> None:
    _execute(state, llm, "PRECEDENT_RESEARCH", P.PRECEDENT_SYSTEM,
             P.user_with_context("Find similar/landmark precedents and rank by relevance.", state.context))


# ---- Agent 5: Argument Builder ----
def argument_builder(state: AgentState, llm: LLMProvider) -> None:
    data = _execute(state, llm, "ARGUMENT_BUILDER", P.ARGUMENT_SYSTEM,
                    P.user_with_context("Build petitioner and respondent arguments.", state.context))
    for a in data.get("arguments", []):
        side = (a.get("partySide") or "").upper()
        if side not in ("PETITIONER", "RESPONDENT"):
            continue
        state.arguments.append(ArgumentPayload(
            party_side=side,
            argument_text=a.get("argumentText", ""),
            strength=(a.get("strength") or None),
            source_excerpt=a.get("sourceExcerpt"),
        ))


# ---- Agent 6: Risk Analysis (feeds risks + case strength) ----
def risk_analysis(state: AgentState, llm: LLMProvider) -> None:
    data = _execute(state, llm, "RISK_ANALYSIS", P.RISK_SYSTEM,
                    P.user_with_context("Identify weaknesses, contradictions, missing documents.", state.context))
    for r in data.get("risks", []):
        state.risks.append(RiskPayload(
            risk_type=(r.get("riskType") or "DOCUMENTATION").upper(),
            severity=(r.get("severity") or None),
            description=r.get("description", ""),
        ))
    cs = data.get("caseStrength")
    if isinstance(cs, dict):
        state.case_strength = CaseStrengthPayload(
            overall_score=cs.get("overall"),
            strong=cs.get("strong", []),
            weak=cs.get("weak", []),
            missing_evidence=cs.get("missingEvidence", []),
            open_questions=cs.get("openQuestions", []),
        )


# ---- Agent 7: Judge Perspective (telemetry-only) ----
def judge_perspective(state: AgentState, llm: LLMProvider) -> None:
    _execute(state, llm, "JUDGE_PERSPECTIVE", P.JUDGE_SYSTEM,
             P.user_with_context("Anticipate the bench's key questions and concerns.", state.context))


# ---- IRAC Composer (not one of the 7 enum agents → no agent_execution row) ----
def irac_composer(state: AgentState, llm: LLMProvider) -> None:
    issues = [i.issue_text for i in state.issues]
    try:
        result = llm.complete(P.IRAC_SYSTEM, P.irac_user(state.context, issues))
        state.record_usage(result)
        data = parse_json(result.text)
        for ir in data.get("irac", []):
            state.irac.append(IracPayload(
                issue=ir.get("issue", ""),
                rule=ir.get("rule", ""),
                application=ir.get("application", ""),
                conclusion=ir.get("conclusion", ""),
            ))
    except Exception:  # noqa: BLE001
        pass


# ---- Analytics synthesis (deterministic; no LLM call → no agent_execution row) ----
def analytics_synthesis(state: AgentState, llm: LLMProvider) -> None:
    """Derive litigation-readiness from the available analysis signals (Analytics Center)."""
    established = sum(1 for f in state.facts if f.fact_status == "ESTABLISHED")
    missing = sum(1 for f in state.facts if f.fact_status == "MISSING")
    disputed = sum(1 for f in state.facts if f.fact_status == "DISPUTED")

    evidence_readiness = _clamp((established + 1) / (established + missing + disputed + 2))
    research_readiness = _clamp(len(state.issues) / 5.0)
    hearing_readiness = _clamp(len(state.arguments) / 6.0)
    # Witnesses are produced by a later agent; approximate from established facts for now.
    witness_readiness = _clamp(established / 6.0)
    overall = _clamp((evidence_readiness + research_readiness + hearing_readiness + witness_readiness) / 4)

    state.readiness = ReadinessPayload(
        evidence_readiness=evidence_readiness,
        witness_readiness=witness_readiness,
        research_readiness=research_readiness,
        hearing_readiness=hearing_readiness,
        overall_readiness=overall,
    )
    # Fallback case-strength if the risk agent did not provide one.
    if state.case_strength is None:
        state.case_strength = CaseStrengthPayload(overall_score=overall)


# Topological order honoring the dependency graph (AI architecture §4).
PIPELINE = [
    fact_extraction,
    issue_identification,
    statute_analysis,
    argument_builder,
    precedent_research,
    risk_analysis,
    judge_perspective,
    irac_composer,
    analytics_synthesis,
]
