from __future__ import annotations

from dataclasses import dataclass

from app.config import get_settings


@dataclass
class Chunk:
    text: str
    index: int
    page: int | None = None


def chunk_text(text: str, page: int | None = None) -> list[Chunk]:
    """
    Sliding-window chunking (~max_chunk_tokens with overlap), approximating tokens at 4
    chars/token so citations stay coherent (AI architecture §2).
    """
    s = get_settings()
    size = max(200, s.max_chunk_tokens * 4)
    overlap = min(size // 2, s.chunk_overlap_tokens * 4)
    step = max(1, size - overlap)

    cleaned = " ".join(text.split())
    if not cleaned:
        return []

    chunks: list[Chunk] = []
    start = 0
    idx = 0
    while start < len(cleaned):
        chunks.append(Chunk(text=cleaned[start : start + size], index=idx, page=page))
        idx += 1
        start += step
    return chunks
