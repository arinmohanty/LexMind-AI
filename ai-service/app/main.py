import logging

from fastapi import FastAPI

from app import __version__
from app.api import routes_analyze, routes_chat, routes_health, routes_process

logging.basicConfig(level=logging.INFO)

app = FastAPI(
    title="LexMind AI Service",
    version=__version__,
    description="Document pipeline, 7-agent legal analysis (LangGraph), and RAG chat. "
    "Outputs are analytical aids, not legal advice.",
)

app.include_router(routes_health.router)
app.include_router(routes_analyze.router)
app.include_router(routes_process.router)
app.include_router(routes_chat.router)


@app.get("/", tags=["health"])
def root() -> dict:
    return {"service": "lexmind-ai", "version": __version__, "docs": "/docs"}
