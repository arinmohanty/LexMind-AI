from fastapi import APIRouter, Depends

from app.rag.chat import answer_question
from app.schemas import ChatAnswer, ChatRequest
from app.security import require_internal_token

router = APIRouter(tags=["chat"], dependencies=[Depends(require_internal_token)])


@router.post("/chat", response_model=ChatAnswer)
def chat(req: ChatRequest) -> ChatAnswer:
    """Grounded RAG answer over a case's indexed documents."""
    return answer_question(req.case_id, req.question, req.history)
