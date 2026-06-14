"""
Text extraction from raw document bytes (AI architecture §2). Heavy parsers are lazily
imported so the service runs with only core deps until a real document is processed.
Returns (text, page_count, ocr_applied).
"""
from __future__ import annotations

import io


def extract_text(content: bytes, mime_type: str, filename: str = "") -> tuple[str, int, bool]:
    mime = (mime_type or "").lower()
    name = (filename or "").lower()

    if mime == "application/pdf" or name.endswith(".pdf"):
        return _extract_pdf(content)
    if "wordprocessingml" in mime or name.endswith(".docx"):
        return _extract_docx(content), 1, False
    if mime.startswith("image/") or name.endswith((".png", ".jpg", ".jpeg", ".tiff")):
        return _extract_image(content), 1, True
    # Fallback: treat as text
    return content.decode("utf-8", errors="ignore"), 1, False


def _extract_pdf(content: bytes) -> tuple[str, int, bool]:
    try:
        from pypdf import PdfReader
    except ImportError as exc:  # pragma: no cover
        raise RuntimeError("pypdf is required for PDF extraction (pip install pypdf)") from exc

    reader = PdfReader(io.BytesIO(content))
    pages = [page.extract_text() or "" for page in reader.pages]
    text = "\n".join(pages).strip()
    # Scanned PDFs yield little text → OCR fallback.
    if len(text) < 40 and reader.pages:
        return _ocr_pdf(content), len(reader.pages), True
    return text, len(reader.pages), False


def _ocr_pdf(content: bytes) -> str:  # pragma: no cover - requires native deps
    try:
        import pytesseract
        from pdf2image import convert_from_bytes
    except ImportError as exc:
        raise RuntimeError("OCR for scanned PDFs needs pytesseract + pdf2image") from exc
    images = convert_from_bytes(content)
    return "\n".join(pytesseract.image_to_string(img) for img in images).strip()


def _extract_docx(content: bytes) -> str:
    try:
        import docx
    except ImportError as exc:  # pragma: no cover
        raise RuntimeError("python-docx is required for DOCX extraction") from exc
    document = docx.Document(io.BytesIO(content))
    return "\n".join(p.text for p in document.paragraphs).strip()


def _extract_image(content: bytes) -> str:  # pragma: no cover - requires native deps
    try:
        import pytesseract
        from PIL import Image
    except ImportError as exc:
        raise RuntimeError("pytesseract + pillow are required for image OCR") from exc
    return pytesseract.image_to_string(Image.open(io.BytesIO(content))).strip()
