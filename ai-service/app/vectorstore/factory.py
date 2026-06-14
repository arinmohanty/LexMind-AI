from __future__ import annotations

from functools import lru_cache

from app.config import get_settings
from app.vectorstore.base import VectorStore
from app.vectorstore.memory import InMemoryVectorStore


@lru_cache
def get_vector_store() -> VectorStore:
    s = get_settings()
    if s.qdrant_url:
        from app.vectorstore.qdrant_store import QdrantVectorStore

        return QdrantVectorStore(s.qdrant_url, s.qdrant_api_key, s.embedding_dim)
    return InMemoryVectorStore()
