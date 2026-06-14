from __future__ import annotations

import logging

from app.agents import nodes
from app.agents.state import AgentState, build_context
from app.llm.base import LLMProvider
from app.llm.factory import get_llm
from app.schemas import AgentResultsPayload

logger = logging.getLogger(__name__)


def run_analysis(
    case_id: str,
    documents: list[tuple[str | None, str]],
    llm: LLMProvider | None = None,
    *,
    use_graph: bool | None = None,
) -> AgentResultsPayload:
    """
    Run the multi-agent analysis for a case. `documents` is a list of (filename, text).
    Prefers the LangGraph graph when available; falls back to a sequential executor.
    """
    llm = llm or get_llm()
    state = AgentState(case_id=case_id, context=build_context(documents))

    if use_graph is None:
        from app.agents.graph import langgraph_available
        use_graph = langgraph_available()

    if use_graph:
        try:
            from app.agents.graph import run_with_graph
            run_with_graph(state, llm)
            return state.to_payload()
        except Exception as exc:  # noqa: BLE001
            logger.warning("LangGraph execution failed (%s); using sequential runner", exc)

    _run_sequential(state, llm)
    return state.to_payload()


def _run_sequential(state: AgentState, llm: LLMProvider) -> None:
    for node in nodes.PIPELINE:
        node(state, llm)
