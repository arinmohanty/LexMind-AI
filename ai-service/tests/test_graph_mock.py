"""
Offline verification of the agent graph + RAG with the deterministic mock provider.
Runs with pytest OR directly: `python tests/test_graph_mock.py`.
"""
from app.agents.runner import run_analysis
from app.llm.mock import MockLLM
from app.pipeline.ingest import ingest_text
from app.rag.chat import answer_question
from app.schemas import AgentResultsPayload

SAMPLE = [
    ("judgment.pdf",
     "FIR No. 452 was registered. PW1 deposed the accused was present at ~9:30 PM. "
     "A weapon was recovered. The defence pleads self-defence."),
]


def _payload() -> AgentResultsPayload:
    return run_analysis("case-1", SAMPLE, llm=MockLLM(), use_graph=False)


def test_run_analysis_produces_full_payload():
    p = _payload()
    assert p.facts, "expected facts"
    assert p.timeline, "expected timeline"
    assert p.issues, "expected issues"
    assert p.arguments, "expected arguments"
    assert p.irac, "expected IRAC"
    assert len(p.agent_executions) == 7, "expected all 7 agents to record an execution"
    assert all(a.status == "COMPLETED" for a in p.agent_executions)


def test_run_analysis_produces_analytics():
    p = _payload()
    assert p.risks, "expected risk findings"
    assert p.case_strength is not None and p.case_strength.overall_score is not None
    assert p.readiness is not None
    r = p.readiness
    for score in (r.evidence_readiness, r.research_readiness, r.hearing_readiness,
                  r.witness_readiness, r.overall_readiness):
        assert score is None or 0.0 <= score <= 1.0


def test_payload_serializes_to_camel_case_contract():
    dumped = _payload().model_dump(by_alias=True)
    assert {"totalTokens", "costUsd", "agentExecutions"} <= dumped.keys()
    assert "factText" in dumped["facts"][0]
    assert "factStatus" in dumped["facts"][0]
    assert dumped["arguments"][0]["partySide"] in ("PETITIONER", "RESPONDENT")
    assert {"issue", "rule", "application", "conclusion"} <= dumped["irac"][0].keys()
    assert dumped["agentExecutions"][0]["agentType"]


def test_rag_chat_is_grounded():
    ingest_text("case-2", "doc-1",
                "PW1 stated the incident occurred around 9:30 PM. The FIR records about 11 PM.")
    ans = answer_question("case-2", "What time did PW1 mention?", [], llm=MockLLM())
    assert ans.answer
    assert ans.confidence in ("High", "Medium", "Low", None)


if __name__ == "__main__":
    test_run_analysis_produces_full_payload()
    test_run_analysis_produces_analytics()
    test_payload_serializes_to_camel_case_contract()
    test_rag_chat_is_grounded()
    p = _payload()
    print("OK — agents:", len(p.agent_executions),
          "| facts:", len(p.facts),
          "| issues:", len(p.issues),
          "| arguments:", len(p.arguments),
          "| irac:", len(p.irac),
          "| risks:", len(p.risks),
          "| readiness:", round(p.readiness.overall_readiness or 0, 2),
          "| tokens:", p.total_tokens)
