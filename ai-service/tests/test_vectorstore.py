from app.vectorstore.base import VectorRecord, cosine
from app.vectorstore.memory import InMemoryVectorStore


def test_cosine_identical_is_one():
    assert abs(cosine([1.0, 0.0], [1.0, 0.0]) - 1.0) < 1e-9


def test_in_memory_search_is_case_scoped():
    store = InMemoryVectorStore()
    store.upsert([
        VectorRecord(id="a", case_id="c1", text="alpha", embedding=[1.0, 0.0, 0.0]),
        VectorRecord(id="b", case_id="c1", text="beta", embedding=[0.0, 1.0, 0.0]),
        VectorRecord(id="x", case_id="c2", text="other", embedding=[1.0, 0.0, 0.0]),
    ])
    hits = store.search("c1", [1.0, 0.0, 0.0], top_k=5)
    assert [h.id for h in hits][0] == "a"
    assert all(h.case_id == "c1" for h in hits)  # never leaks c2


def test_delete_case_removes_only_that_case():
    store = InMemoryVectorStore()
    store.upsert([
        VectorRecord(id="a", case_id="c1", text="x", embedding=[1.0]),
        VectorRecord(id="b", case_id="c2", text="y", embedding=[1.0]),
    ])
    store.delete_case("c1")
    assert store.search("c1", [1.0], 5) == []
    assert len(store.search("c2", [1.0], 5)) == 1
