"""Qdrant-backed store (ADR-0001). Lazily imports qdrant-client. Payload-filters by caseId."""
from __future__ import annotations

from app.vectorstore.base import VectorRecord

COLLECTION = "case_chunks"


class QdrantVectorStore:
    def __init__(self, url: str, api_key: str, dim: int) -> None:
        from qdrant_client import QdrantClient
        from qdrant_client.models import Distance, VectorParams

        self._client = QdrantClient(url=url, api_key=api_key or None)
        self._dim = dim
        existing = {c.name for c in self._client.get_collections().collections}
        if COLLECTION not in existing:
            self._client.create_collection(
                collection_name=COLLECTION,
                vectors_config=VectorParams(size=dim, distance=Distance.COSINE),
            )

    def upsert(self, records: list[VectorRecord]) -> None:
        from qdrant_client.models import PointStruct

        points = [
            PointStruct(
                id=r.id,
                vector=r.embedding or [],
                payload={
                    "caseId": r.case_id,
                    "documentId": r.document_id,
                    "text": r.text,
                    "page": r.page,
                },
            )
            for r in records
        ]
        self._client.upsert(collection_name=COLLECTION, points=points)

    def search(self, case_id: str, query: list[float], top_k: int) -> list[VectorRecord]:
        from qdrant_client.models import FieldCondition, Filter, MatchValue

        hits = self._client.search(
            collection_name=COLLECTION,
            query_vector=query,
            limit=top_k,
            query_filter=Filter(must=[FieldCondition(key="caseId", match=MatchValue(value=case_id))]),
        )
        return [
            VectorRecord(
                id=str(h.id),
                case_id=case_id,
                text=(h.payload or {}).get("text", ""),
                document_id=(h.payload or {}).get("documentId"),
                page=(h.payload or {}).get("page"),
            )
            for h in hits
        ]

    def delete_case(self, case_id: str) -> None:
        from qdrant_client.models import FieldCondition, Filter, MatchValue

        self._client.delete(
            collection_name=COLLECTION,
            points_selector=Filter(must=[FieldCondition(key="caseId", match=MatchValue(value=case_id))]),
        )
