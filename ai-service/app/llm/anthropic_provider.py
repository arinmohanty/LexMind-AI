"""Real Claude provider (ADR-0008). Used when LLM_PROVIDER=anthropic and a key is set."""
from __future__ import annotations

from app.llm.base import LLMResult


class AnthropicLLM:
    def __init__(self, api_key: str, model: str) -> None:
        import anthropic  # lazy import; only required when this provider is selected

        self._client = anthropic.Anthropic(api_key=api_key)
        self.model = model

    def complete(self, system: str, user: str, *, max_tokens: int = 2048,
                 temperature: float = 0.2) -> LLMResult:
        message = self._client.messages.create(
            model=self.model,
            system=system,
            max_tokens=max_tokens,
            temperature=temperature,
            messages=[{"role": "user", "content": user}],
        )
        text = "".join(block.text for block in message.content if block.type == "text")
        return LLMResult(
            text=text,
            input_tokens=message.usage.input_tokens,
            output_tokens=message.usage.output_tokens,
            model=self.model,
        )
