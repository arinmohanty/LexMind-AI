from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status

from app.pipeline.documents import BackendDocumentProvider
from app.pipeline.ingest import ingest_text
from app.schemas import ProcessRequest, ProcessResponse
from app.security import require_internal_token

router = APIRouter(tags=["documents"], dependencies=[Depends(require_internal_token)])


@router.post("/process", response_model=ProcessResponse)
def process(req: ProcessRequest) -> ProcessResponse:
    """OCR/parse/chunk/embed a single document into the vector store."""
    if req.text is not None:
        text, pages, ocr = req.text, 1, False
    else:
        docs = BackendDocumentProvider().get_documents([req.document_id])
        if not docs:
            raise HTTPException(status.HTTP_404_NOT_FOUND, "Document content unavailable")
        text, pages, ocr = docs[0][2], 1, False

    chunks = ingest_text(req.case_id, req.document_id, text)
    return ProcessResponse(
        document_id=req.document_id, chunks=chunks, pages=pages, ocr_applied=ocr
    )
