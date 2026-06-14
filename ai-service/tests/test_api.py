"""API + security tests via FastAPI TestClient (covers routes, main, internal-token guard)."""
from fastapi.testclient import TestClient

from app.config import get_settings
from app.main import app

client = TestClient(app)
TOKEN = {"X-Internal-Token": get_settings().internal_service_token}


def test_health_and_ready_are_public():
    assert client.get("/health").json()["status"] == "UP"
    body = client.get("/ready").json()
    assert body["status"] == "READY"
    assert client.get("/").json()["service"] == "lexmind-ai"


def test_analyze_requires_internal_token():
    resp = client.post("/analyze", json={"caseId": "c1", "documentIds": []})
    assert resp.status_code == 401


def test_analyze_with_inline_documents_returns_payload():
    resp = client.post(
        "/analyze",
        headers=TOKEN,
        json={
            "caseId": "api-case-1",
            "documentIds": [],
            "documents": [{"id": "d1", "filename": "judgment.pdf",
                           "text": "FIR 452. PW1 saw the accused. A weapon was recovered."}],
        },
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["facts"] and body["issues"] and body["arguments"]
    assert body["agentExecutions"]
    assert body["readiness"] is not None


def test_process_indexes_text_then_chat_answers():
    pr = client.post(
        "/process",
        headers=TOKEN,
        json={"caseId": "api-case-2", "documentId": "d2",
              "text": "PW1 stated the incident occurred around 9:30 PM."},
    )
    assert pr.status_code == 200
    assert pr.json()["chunks"] >= 1

    cr = client.post(
        "/chat",
        headers=TOKEN,
        json={"caseId": "api-case-2", "question": "What time did PW1 mention?", "history": []},
    )
    assert cr.status_code == 200
    assert cr.json()["answer"]
