from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """12-factor configuration (ADR-0008). Read from environment / .env."""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore", case_sensitive=False)

    # LLM
    llm_provider: str = "mock"          # mock | anthropic
    anthropic_api_key: str = ""
    llm_model_strong: str = "claude-opus-4-8"
    llm_model_fast: str = "claude-haiku-4-5-20251001"

    # Embeddings
    embedding_provider: str = "mock"
    embedding_dim: int = 384

    # Vector store
    qdrant_url: str = ""                 # empty -> in-memory store
    qdrant_api_key: str = ""

    # Service-to-service
    internal_service_token: str = "dev-internal-service-token"
    backend_base_url: str = "http://localhost:8080"

    # Pipeline
    max_chunk_tokens: int = 800
    chunk_overlap_tokens: int = 100
    retrieval_top_k: int = 8


@lru_cache
def get_settings() -> Settings:
    return Settings()
