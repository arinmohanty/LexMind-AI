from __future__ import annotations

import logging
from uuid import uuid4

from fastapi import APIRouter, Depends

from app.agents.runner import run_analysis
from app.pipeline.documents import BackendDocumentProvider
from app.pipeline.ingest import ingest_text
from app.schemas import AgentResultsPayload, RunAgentsRequest
from app.security import require_internal_token

logger = logging.getLogger(__name__)
router = APIRouter(tags=["analysis"], dependencies=[Depends(require_internal_token)])


@router.post("/analyze", response_model=AgentResultsPayload)
def analyze(req: RunAgentsRequest) -> AgentResultsPayload:
    """Run the 7-agent pipeline for a case and return the structured payload the backend ingests."""
    # Resolve document text: inline (tests) or fetched from the backend.
    if req.documents:
        resolved = [(d.id or str(uuid4()), d.filename, d.text) for d in req.documents]
    else:
        resolved = BackendDocumentProvider().get_documents(req.document_ids)

    # Index chunks so the case becomes chat-able (RAG), tolerating failures.
    for doc_id, _filename, text in resolved:
        try:
            ingest_text(req.case_id, doc_id, text)
        except Exception as exc:  # noqa: BLE001
            logger.warning("Ingest failed for document %s: %s", doc_id, exc)

    return run_analysis(req.case_id, [(filename, text) for _id, filename, text in resolved])
