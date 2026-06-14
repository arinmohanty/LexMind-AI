from __future__ import annotations

from uuid import uuid4

from app.llm.base import EmbeddingProvider
from app.llm.factory import get_embedding
from app.pipeline.chunk import chunk_text
from app.vectorstore.base import VectorRecord, VectorStore
from app.vectorstore.factory import get_vector_store


def ingest_text(
    case_id: str,
    document_id: str,
    text: str,
    *,
    store: VectorStore | None = None,
    emb: EmbeddingProvider | None = None,
) -> int:
    """Chunk → embed → upsert a document's text into the vector store. Returns chunk count."""
    chunks = chunk_text(text)
    if not chunks:
        return 0
    emb = emb or get_embedding()
    store = store or get_vector_store()
    vectors = emb.embed([c.text for c in chunks])
    records = [
        VectorRecord(
            id=str(uuid4()),
            case_id=case_id,
            document_id=document_id,
            text=c.text,
            page=c.page,
            embedding=v,
        )
        for c, v in zip(chunks, vectors)
    ]
    store.upsert(records)
    return len(records)
