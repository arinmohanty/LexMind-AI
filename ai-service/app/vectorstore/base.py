from __future__ import annotations

from dataclasses import dataclass
from typing import Protocol


@dataclass
class VectorRecord:
    id: str
    case_id: str
    text: str
    document_id: str | None = None
    page: int | None = None
    embedding: list[float] | None = None


class VectorStore(Protocol):
    """Case-scoped vector store. All searches MUST filter by case_id (isolation, AI arch §5)."""

    def upsert(self, records: list[VectorRecord]) -> None: ...

    def search(self, case_id: str, query: list[float], top_k: int) -> list[VectorRecord]: ...

    def delete_case(self, case_id: str) -> None: ...


def cosine(a: list[float], b: list[float]) -> float:
    dot = sum(x * y for x, y in zip(a, b))
    na = sum(x * x for x in a) ** 0.5 or 1.0
    nb = sum(y * y for y in b) ** 0.5 or 1.0
    return dot / (na * nb)
