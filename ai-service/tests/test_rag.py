from app.llm.mock import MockLLM
from app.pipeline.ingest import ingest_text
from app.rag.chat import answer_question
from app.vectorstore.memory import InMemoryVectorStore


def test_chat_returns_citations_from_indexed_chunks():
    store = InMemoryVectorStore()
    n = ingest_text("case-rag", "doc-1",
                    "PW1 stated the incident occurred around 9:30 PM. The FIR records about 11 PM.",
                    store=store)
    assert n >= 1
    answer = answer_question("case-rag", "What time did PW1 mention?", [],
                             llm=MockLLM(), store=store)
    assert answer.answer
    assert len(answer.citations) >= 1
    assert answer.citations[0].document_id == "doc-1"


def test_chat_with_no_index_is_low_confidence():
    store = InMemoryVectorStore()
    answer = answer_question("empty-case", "anything?", [], llm=MockLLM(), store=store)
    assert answer.citations == []
    assert answer.confidence == "Low"
