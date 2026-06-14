from __future__ import annotations

from app.vectorstore.base import VectorRecord, cosine


class InMemoryVectorStore:
    """Dev/offline fallback when QDRANT_URL is unset. Not for production scale."""

    def __init__(self) -> None:
        self._records: dict[str, VectorRecord] = {}

    def upsert(self, records: list[VectorRecord]) -> None:
        for r in records:
            self._records[r.id] = r

    def search(self, case_id: str, query: list[float], top_k: int) -> list[VectorRecord]:
        scored = [
            (cosine(query, r.embedding or []), r)
            for r in self._records.values()
            if r.case_id == case_id and r.embedding
        ]
        scored.sort(key=lambda t: t[0], reverse=True)
        return [r for _, r in scored[:top_k]]

    def delete_case(self, case_id: str) -> None:
        for key in [k for k, v in self._records.items() if v.case_id == case_id]:
            del self._records[key]
