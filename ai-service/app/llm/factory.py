from __future__ import annotations

from functools import lru_cache

from app.config import get_settings
from app.llm.base import EmbeddingProvider, LLMProvider
from app.llm.mock import MockEmbedding, MockLLM


@lru_cache
def get_llm() -> LLMProvider:
    s = get_settings()
    if s.llm_provider == "anthropic" and s.anthropic_api_key:
        from app.llm.anthropic_provider import AnthropicLLM

        return AnthropicLLM(s.anthropic_api_key, s.llm_model_strong)
    return MockLLM()


@lru_cache
def get_embedding() -> EmbeddingProvider:
    s = get_settings()
    # A real embedding provider (e.g. Voyage / sentence-transformers) can be added here.
    return MockEmbedding(s.embedding_dim)
