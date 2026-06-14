"""
LangGraph wiring of the 7-agent pipeline (ADR-0004). Used in production when langgraph is
installed; the runner falls back to the sequential executor otherwise. The graph encodes the
dependency edges from the AI architecture: Fact → Issue → {Statute, Argument} →
{Precedent, Risk} → Judge → IRAC → aggregate.
"""
from __future__ import annotations

from app.agents import nodes
from app.agents.state import AgentState
from app.llm.base import LLMProvider


def langgraph_available() -> bool:
    try:
        import langgraph  # noqa: F401
        return True
    except Exception:
        return False


def run_with_graph(state: AgentState, llm: LLMProvider) -> AgentState:
    """Execute the pipeline as a LangGraph StateGraph. Returns the populated state."""
    from langgraph.graph import END, START, StateGraph

    builder: StateGraph = StateGraph(AgentState)

    def wrap(fn):
        def _node(s: AgentState) -> AgentState:
            fn(s, llm)
            return s
        return _node

    builder.add_node("facts", wrap(nodes.fact_extraction))
    builder.add_node("issues", wrap(nodes.issue_identification))
    builder.add_node("statutes", wrap(nodes.statute_analysis))
    builder.add_node("arguments", wrap(nodes.argument_builder))
    builder.add_node("precedents", wrap(nodes.precedent_research))
    builder.add_node("risk", wrap(nodes.risk_analysis))
    builder.add_node("judge", wrap(nodes.judge_perspective))
    builder.add_node("irac", wrap(nodes.irac_composer))

    builder.add_edge(START, "facts")
    builder.add_edge("facts", "issues")
    builder.add_edge("issues", "statutes")
    builder.add_edge("issues", "arguments")
    builder.add_edge("statutes", "precedents")
    builder.add_edge("arguments", "risk")
    builder.add_edge("precedents", "judge")
    builder.add_edge("risk", "judge")
    builder.add_edge("judge", "irac")
    builder.add_edge("irac", END)

    graph = builder.compile()
    return graph.invoke(state)
