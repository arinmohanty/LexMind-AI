# LexMind AI

> Production-grade full-stack platform for legal intelligence, case analysis, and AI-assisted litigation workflow.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=flat&logo=react)](https://react.dev/)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?style=flat&logo=python)](https://python.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker)](https://www.docker.com/)

---

## The Problem It Solves

Legal work is often buried in fragmented documents, inconsistent case notes, and disconnected research workflows. Most existing tools are either:

- basic document stores with no analytical depth
- generic chat interfaces with weak grounding
- rigid systems that do not support multi-step case reasoning

LexMind AI closes that gap. It turns raw legal material into structured, intelligent case analysis with a modern software architecture behind it.

---

## Why This Project Stands Out

### Engineering Depth

This is not a toy prototype. The system is designed as a modular, production-minded platform with:

- a secure backend for users, cases, permissions, and orchestration
- a React-based frontend for workflow-driven interaction and analytics
- a Python AI service for document processing, retrieval, and agent-based reasoning
- containerized deployment for local and production-style environments

### Technical Choices

The architecture is intentionally split across three services to keep concerns isolated while preserving strong integration:

| Layer | Choice | Why It Matters |
|---|---|---|
| Frontend | React + TypeScript + Vite | Fast iteration, strong typing, modern DX |
| Backend | Java 21 + Spring Boot 3 | Enterprise-grade APIs, strong validation, scalable service design |
| AI Service | Python + FastAPI + LangGraph | Flexible orchestration for agent workflows and RAG |
| Data | PostgreSQL + Qdrant | Relational persistence plus vector search |
| Delivery | Docker Compose | Consistent local deployment and environment parity |

---

## Core Capabilities

### Case Intelligence Platform
- document ingestion for legal files and evidence material
- structured workspace for case-related analysis
- role-based access control and secure authentication

### AI-Powered Analysis
- multi-agent analysis graph for fact, issue, argument, and IRAC-style reasoning
- retrieval-augmented chat grounded in uploaded documents
- dashboard generation for case insights and analysis results

### Product Experience
- modern, responsive interface for legal workflows
- upload-to-analysis lifecycle for end-to-end case handling
- clean separation between UI, business logic, and AI orchestration

---

## Architecture

```text
┌──────────────────────────────────────────────┐
│                  Frontend                    │
│ React + TypeScript + Vite + Tailwind CSS     │
└──────────────────────┬───────────────────────┘
                       │ REST APIs
                       ▼
┌──────────────────────────────────────────────┐
│                   Backend                     │
│ Java 21 + Spring Boot + Spring Security     │
│ Auth, RBAC, cases, documents, orchestration │
└──────────────────────┬───────────────────────┘
                       │ async integration
                       ▼
┌──────────────────────────────────────────────┐
│                 AI Service                   │
│ Python + FastAPI + LangGraph + RAG          │
│ document processing, retrieval, analysis     │
└──────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React, TypeScript, Vite, Tailwind CSS, React Query, React Router, Recharts |
| Backend | Java 21, Spring Boot 3, Spring Security, JWT, Spring Data JPA |
| AI Service | Python, FastAPI, LangGraph, RAG, vector search |
| Data Stores | PostgreSQL, Qdrant |
| Document Processing | OCR, PDF/DOCX parsing, chunking, embeddings |
| DevOps | Docker, Docker Compose |
| API Layer | OpenAPI / Swagger UI |

---

## Repository Structure

```text
lexmind-ai/
├── frontend/      # React-based application shell and workflows
├── backend/       # Spring Boot API, security, persistence, orchestration
├── ai-service/    # FastAPI AI service with LangGraph and RAG
├── infra/         # Docker Compose and deployment assets
└── docs/          # Product, architecture, UX, and delivery documentation
```

---

## Quick Start

### Full-stack local run with Docker

```bash
cd infra
docker compose up --build
```

Once running:

- Frontend: http://localhost:8081
- Backend API: http://localhost:8080/swagger-ui.html
- AI Service: http://localhost:8000/docs

### Individual services

- Frontend: see [frontend/README.md](frontend/README.md)
- Backend: see [backend/README.md](backend/README.md)
- AI Service: see [ai-service/README.md](ai-service/README.md)

---

## Delivery Roadmap

| Phase | Focus | Status |
|---|---|---|
| 1 | Product definition and requirements | ✅ Completed |
| 2 | Architecture, data design, and AI design | ✅ Completed |
| 3 | UI/UX system and wireframes | ✅ Completed |
| 4 | Backend platform and APIs | ✅ Core implemented |
| 5 | Frontend experience and dashboards | ✅ Core implemented |
| 6 | AI agents, analysis graph, and RAG | ✅ Implemented |
| 7 | Analytics and intelligence surfaces | ✅ Implemented |
| 8 | Testing and reliability | 🟡 In progress |
| 9 | Deployment and containerization | ✅ Completed |
| 10 | Documentation and capstone deliverables | ✅ Completed |

---

## Documentation

- [Problem Statement](docs/phase-01-product/01-problem-statement.md)
- [Market Analysis](docs/phase-01-product/02-market-analysis.md)
- [PRD](docs/phase-01-product/05-prd.md)
- [Architecture Overview](docs/phase-02-architecture/01-architecture-overview.md)
- [AI Architecture](docs/phase-02-architecture/05-ai-architecture.md)
- [Deployment Guide](docs/phase-09-deployment/deployment.md)

---

LexMind AI is built with precision, structure, and a strong engineering mindset — blending legal domain intelligence with modern software architecture and AI orchestration.
