"""
Deterministic mock providers — no API key, no network. They let the full agent graph,
RAG, and tests run offline and produce a valid, contract-shaped payload. Each agent embeds
an "[AGENT:<TYPE>]" marker in its system prompt; the mock returns canned structured JSON for
that marker. Swap to the real Claude provider by setting LLM_PROVIDER=anthropic.
"""
from __future__ import annotations

import hashlib
import json
import struct

from app.llm.base import LLMResult, approx_tokens

# Canned, plausible analysis for a generic criminal matter. Shapes match what the nodes parse.
_CANNED: dict[str, dict] = {
    "FACT_EXTRACTION": {
        "facts": [
            {"factText": "The accused was present at the scene on the date of the incident.",
             "factStatus": "ESTABLISHED", "sourceExcerpt": "PW1 deposed that the accused was seen at ~9:30 PM",
             "confidence": 0.9},
            {"factText": "The weapon allegedly belonged to the accused.",
             "factStatus": "DISPUTED", "sourceExcerpt": "Defence denies ownership of the weapon",
             "confidence": 0.55},
            {"factText": "CCTV footage for the relevant window is not on record.",
             "factStatus": "MISSING", "sourceExcerpt": None, "confidence": 0.8},
        ],
        "timeline": [
            {"eventDate": "2024-01-12", "eventText": "Alleged incident / assault", "eventType": "INCIDENT"},
            {"eventDate": "2024-01-13", "eventText": "FIR No. 452 registered", "eventType": "FIR"},
            {"eventDate": "2024-02-02", "eventText": "Statements recorded during investigation",
             "eventType": "INVESTIGATION"},
            {"eventDate": "2024-03-28", "eventText": "Charge sheet filed", "eventType": "CHARGE_SHEET"},
        ],
    },
    "ISSUE_IDENTIFICATION": {
        "issues": [
            {"issueText": "Whether the accused caused hurt with a dangerous weapon.",
             "issueType": "PRIMARY", "importanceScore": 0.92},
            {"issueText": "Whether the requisite intention (mens rea) is established.",
             "issueType": "PRIMARY", "importanceScore": 0.81},
            {"issueText": "Whether the delay in lodging the FIR is fatal to the prosecution.",
             "issueType": "SECONDARY", "importanceScore": 0.6},
        ],
    },
    "STATUTE_ANALYSIS": {
        "statutes": [
            {"actName": "Indian Penal Code", "section": "324",
             "applicability": "Core charge — voluntarily causing hurt by dangerous weapon", "confidence": 0.9},
            {"actName": "Indian Penal Code", "section": "34",
             "applicability": "Common intention of co-accused", "confidence": 0.72},
            {"actName": "Code of Criminal Procedure", "section": "154",
             "applicability": "Registration of FIR; delay analysis", "confidence": 0.68},
        ],
    },
    "ARGUMENT_BUILDER": {
        "arguments": [
            {"partySide": "PETITIONER", "argumentText": "The act was in self-defence with no intent to cause grievous hurt.",
             "strength": "MODERATE"},
            {"partySide": "PETITIONER", "argumentText": "Unexplained delay in the FIR raises doubt on the prosecution story.",
             "strength": "STRONG"},
            {"partySide": "RESPONDENT", "argumentText": "Eyewitness testimony is corroborated by the medical report.",
             "strength": "STRONG"},
            {"partySide": "RESPONDENT", "argumentText": "The weapon was recovered pursuant to the accused's disclosure.",
             "strength": "MODERATE"},
        ],
    },
    "PRECEDENT_RESEARCH": {
        "precedents": [
            {"citedCaseName": "Bhagwan Singh v. State", "citation": "(1976) 1 SCC",
             "relevance": 0.91, "relationship": "SIMILAR"},
            {"citedCaseName": "State of U.P. v. Ramesh", "citation": "(1989) 3 SCC",
             "relevance": 0.78, "relationship": "FOLLOWED"},
        ],
    },
    "RISK_ANALYSIS": {
        "risks": [
            {"riskType": "EVIDENTIARY", "severity": "HIGH", "description": "Absence of CCTV footage weakens corroboration."},
            {"riskType": "PROCEDURAL", "severity": "MEDIUM", "description": "Delay in FIR may be probed by the court."},
        ],
        "caseStrength": {"overall": 0.62, "strong": ["medical report", "recovery"],
                          "weak": ["FIR delay"], "missingEvidence": ["CCTV", "recovery memo"]},
    },
    "JUDGE_PERSPECTIVE": {
        "questions": [
            "Can the prosecution satisfactorily explain the delay in lodging the FIR?",
            "Is the chain of custody of the recovered weapon established?",
            "How reliable is the sole eyewitness on the question of time?",
        ],
    },
    "IRAC": {
        "irac": [
            {"issue": "Whether the accused caused hurt with a dangerous weapon u/s 324 IPC.",
             "rule": "Section 324 IPC requires voluntary causing of hurt by a dangerous weapon.",
             "application": "Medical evidence and recovery support hurt by a weapon; the defence pleads self-defence.",
             "conclusion": "The prosecution is likely to establish s.324 if the eyewitness withstands cross-examination."},
        ],
    },
}


class MockLLM:
    """Returns canned structured JSON keyed by the agent marker in the system prompt."""

    model = "mock-claude"

    def complete(self, system: str, user: str, *, max_tokens: int = 2048,
                 temperature: float = 0.2) -> LLMResult:
        text = self._dispatch(system, user)
        return LLMResult(
            text=text,
            input_tokens=approx_tokens(system) + approx_tokens(user),
            output_tokens=approx_tokens(text),
            model=self.model,
        )

    def _dispatch(self, system: str, user: str) -> str:
        for marker, payload in _CANNED.items():
            if f"[AGENT:{marker}]" in system:
                return json.dumps(payload)
        if "[TASK:CHAT]" in system:
            return json.dumps({
                "answer": "Based on the documents, PW1 placed the incident at ~9:30 PM [1], "
                          "while the FIR records ~11 PM [2] — a possible contradiction on timing.",
                "confidence": "High",
            })
        return json.dumps({"answer": "Insufficient information in the provided documents."})


class MockEmbedding:
    """Deterministic pseudo-embeddings (hash-seeded) so retrieval is reproducible offline."""

    def __init__(self, dim: int = 384) -> None:
        self._dim = dim

    @property
    def dim(self) -> int:
        return self._dim

    def embed(self, texts: list[str]) -> list[list[float]]:
        return [self._vec(t) for t in texts]

    def _vec(self, text: str) -> list[float]:
        out: list[float] = []
        counter = 0
        while len(out) < self._dim:
            h = hashlib.sha256(f"{text}|{counter}".encode()).digest()
            for i in range(0, len(h), 4):
                if len(out) >= self._dim:
                    break
                (val,) = struct.unpack("I", h[i:i + 4])
                out.append((val / 2**32) * 2 - 1)  # in [-1, 1]
            counter += 1
        # L2 normalize
        norm = sum(x * x for x in out) ** 0.5 or 1.0
        return [x / norm for x in out]
