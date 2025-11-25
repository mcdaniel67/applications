"""JWT token utilities."""

import jwt
from datetime import datetime, timedelta
from typing import Dict, Optional
from flask import current_app


def create_access_token(user_id: int, username: str) -> str:
    """
    Create a JWT access token for a user.

    Args:
        user_id: User's ID
        username: User's username

    Returns:
        JWT token as a string
    """
    payload = {
        "user_id": user_id,
        "username": username,
        "exp": datetime.utcnow() + timedelta(hours=24),
        "iat": datetime.utcnow(),
    }
    token = jwt.encode(payload, current_app.config["SECRET_KEY"], algorithm="HS256")
    return token


def decode_access_token(token: str) -> Optional[Dict]:
    """
    Decode a JWT access token.

    Args:
        token: JWT token string

    Returns:
        Decoded payload dict if valid, None if invalid or expired
    """
    try:
        payload = jwt.decode(
            token, current_app.config["SECRET_KEY"], algorithms=["HS256"]
        )
        return payload
    except jwt.ExpiredSignatureError:
        return None
    except jwt.InvalidTokenError:
        return None
