from app.pipeline.chunk import chunk_text
from app.pipeline.extract import extract_text


def test_chunking_splits_long_text_with_overlap():
    text = "word " * 2000
    chunks = chunk_text(text)
    assert len(chunks) > 1
    assert all(c.text for c in chunks)
    assert [c.index for c in chunks] == list(range(len(chunks)))


def test_chunking_empty_text_returns_nothing():
    assert chunk_text("") == []
    assert chunk_text("   ") == []


def test_extract_plain_text_fallback():
    text, pages, ocr = extract_text(b"hello world", "text/plain", "note.txt")
    assert "hello world" in text
    assert pages == 1
    assert ocr is False
