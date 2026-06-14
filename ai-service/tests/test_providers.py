import json

from app.llm.mock import MockEmbedding, MockLLM


def test_mock_llm_dispatches_by_agent_marker():
    llm = MockLLM()
    result = llm.complete("[AGENT:FACT_EXTRACTION]", "extract")
    data = json.loads(result.text)
    assert "facts" in data and "timeline" in data
    assert result.total_tokens > 0


def test_mock_llm_chat_marker():
    llm = MockLLM()
    data = json.loads(llm.complete("[TASK:CHAT]", "q").text)
    assert "answer" in data


def test_mock_embedding_is_deterministic_and_normalized():
    emb = MockEmbedding(dim=64)
    v1 = emb.embed(["hello"])[0]
    v2 = emb.embed(["hello"])[0]
    assert v1 == v2
    assert len(v1) == 64
    norm = sum(x * x for x in v1) ** 0.5
    assert abs(norm - 1.0) < 1e-6
