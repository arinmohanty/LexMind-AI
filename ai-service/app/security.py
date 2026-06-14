from fastapi import Header, HTTPException, status

from app.config import get_settings


async def require_internal_token(x_internal_token: str = Header(default="")) -> None:
    """Authenticate server-to-server calls from the backend (ADR-0002)."""
    if x_internal_token != get_settings().internal_service_token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid internal service token",
        )
