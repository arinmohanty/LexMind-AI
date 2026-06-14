from fastapi import APIRouter

from app.config import get_settings

router = APIRouter(tags=["health"])


@router.get("/health")
def health() -> dict:
    return {"status": "UP"}


@router.get("/ready")
def ready() -> dict:
    s = get_settings()
    return {
        "status": "READY",
        "llmProvider": s.llm_provider,
        "vectorStore": "qdrant" if s.qdrant_url else "in-memory",
    }
