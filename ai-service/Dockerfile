FROM python:3.12-slim

WORKDIR /app
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1

# System deps for OCR (Tesseract) + PDF rasterization (Poppler).
RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr poppler-utils \
    && rm -rf /var/lib/apt/lists/*

# Python deps: core + the optional integrations so Qdrant + LangGraph + OCR work in-cluster.
COPY requirements.txt .
RUN pip install -r requirements.txt \
    && pip install \
        "anthropic==0.34.2" \
        "langgraph==0.2.28" \
        "qdrant-client==1.11.2" \
        "pypdf==4.3.1" \
        "python-docx==1.1.2" \
        "pytesseract==0.3.13" \
        "pillow==10.4.0" \
        "pdf2image==1.17.0"

COPY app ./app

# Non-root user.
RUN useradd --system lexmind && chown -R lexmind /app
USER lexmind

EXPOSE 8000
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=5 \
  CMD ["python", "-c", "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://localhost:8000/health').status==200 else 1)"]
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
