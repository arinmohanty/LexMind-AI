from __future__ import annotations

from app.agents.prompts import CHAT_SYSTEM, chat_user
from app.agents.state import parse_json
from app.config import get_settings
from app.llm.base import EmbeddingProvider, LLMProvider
from app.llm.factory import get_embedding, get_llm
from app.schemas import ChatAnswer, ChatTurn, Citation
from app.vectorstore.base import VectorStore
from app.vectorstore.factory import get_vector_store


def answer_question(
    case_id: str,
    question: str,
    history: list[ChatTurn],
    *,
    llm: LLMProvider | None = None,
    store: VectorStore | None = None,
    emb: EmbeddingProvider | None = None,
) -> ChatAnswer:
    """Grounded RAG answer: retrieve case-scoped chunks, answer with citations (AI arch §3)."""
    llm = llm or get_llm()
    store = store or get_vector_store()
    emb = emb or get_embedding()

    query_vec = emb.embed([question])[0]
    hits = store.search(case_id, query_vec, get_settings().retrieval_top_k)
    passages = [h.text for h in hits]

    result = llm.complete(
        CHAT_SYSTEM,
        chat_user(question, passages, [(t.role, t.content) for t in history]),
    )
    data = parse_json(result.text)
    answer_text = data.get("answer") or result.text

    citations = [
        Citation(
            document_id=h.document_id,
            chunk_id=h.id,
            excerpt=(h.text[:160] if h.text else None),
            page=h.page,
        )
        for h in hits[:3]
    ]
    confidence = data.get("confidence") or ("Low" if not passages else None)
    return ChatAnswer(answer=answer_text, citations=citations, confidence=confidence)
