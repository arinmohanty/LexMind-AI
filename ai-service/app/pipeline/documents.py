"""
Resolves document text for an analysis run. Fetches the original bytes from the backend's
internal content endpoint (service-token auth) and extracts text via the pipeline.
"""
from __future__ import annotations

import logging

import httpx

from app.config import get_settings
from app.pipeline.extract import extract_text

logger = logging.getLogger(__name__)


class BackendDocumentProvider:
    def __init__(self, base_url: str | None = None, token: str | None = None) -> None:
        s = get_settings()
        self.base_url = (base_url or s.backend_base_url).rstrip("/")
        self.token = token or s.internal_service_token

    def get_documents(self, document_ids: list[str]) -> list[tuple[str, str | None, str]]:
        """Returns a list of (document_id, filename, extracted_text)."""
        docs: list[tuple[str, str | None, str]] = []
        with httpx.Client(timeout=60.0) as client:
            for doc_id in document_ids:
                try:
                    resp = client.get(
                        f"{self.base_url}/internal/documents/{doc_id}/content",
                        headers={"X-Internal-Token": self.token},
                    )
                    resp.raise_for_status()
                    filename = resp.headers.get("X-Filename", doc_id)
                    mime = resp.headers.get("content-type", "application/octet-stream")
                    text, _, _ = extract_text(resp.content, mime, filename)
                    docs.append((doc_id, filename, text))
                except Exception as exc:  # noqa: BLE001
                    logger.warning("Failed to fetch/extract document %s: %s", doc_id, exc)
        return docs
