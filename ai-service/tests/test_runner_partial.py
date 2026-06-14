"""Resilience: a single failing agent degrades the run to PARTIAL, never crashes it."""
from app.agents.runner import run_analysis
from app.llm.base import LLMResult
from app.llm.mock import MockLLM


class SelectiveFailLLM:
    model = "mock-claude"

    def __init__(self) -> None:
        self._inner = MockLLM()

    def complete(self, system: str, user: str, *, max_tokens: int = 2048,
                 temperature: float = 0.2) -> LLMResult:
        if "[AGENT:RISK_ANALYSIS]" in system:
            raise RuntimeError("simulated provider timeout")
        return self._inner.complete(system, user, max_tokens=max_tokens, temperature=temperature)


def test_single_agent_failure_is_isolated():
    payload = run_analysis("case-x", [("doc", "some text")], llm=SelectiveFailLLM(), use_graph=False)
    statuses = {a.agent_type: a.status for a in payload.agent_executions}
    assert statuses["RISK_ANALYSIS"] == "FAILED"
    assert statuses["FACT_EXTRACTION"] == "COMPLETED"
    # other agents still produced output
    assert payload.facts and payload.issues and payload.arguments
    # readiness synthesis still runs (deterministic, no LLM)
    assert payload.readiness is not None
