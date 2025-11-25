"""Custom decorators for route protection."""

from functools import wraps
from flask import request, jsonify
from twitter_api.utils.jwt import decode_access_token


def token_required(f):
    """
    Decorator to require JWT token authentication.

    Usage:
        @bp.route('/protected')
        @token_required
        def protected_route(current_user):
            # current_user contains the decoded token payload
            return jsonify({'user_id': current_user['user_id']})
    """

    @wraps(f)
    def decorated(*args, **kwargs):
        token = None

        # Check for token in Authorization header
        if "Authorization" in request.headers:
            auth_header = request.headers["Authorization"]
            try:
                # Expected format: "Bearer <token>"
                token = auth_header.split(" ")[1]
            except IndexError:
                return jsonify({"error": "Invalid token format"}), 401

        if not token:
            return jsonify({"error": "Authentication token is missing"}), 401

        # Decode and validate token
        current_user = decode_access_token(token)
        if current_user is None:
            return jsonify({"error": "Invalid or expired token"}), 401

        # Pass the current_user to the route function
        return f(current_user, *args, **kwargs)

    return decorated


def optional_token(f):
    """
    Decorator for routes where authentication is optional.

    If a valid token is provided, current_user will contain user info.
    If no token or invalid token, current_user will be None.

    Usage:
        @bp.route('/public')
        @optional_token
        def public_route(current_user):
            if current_user:
                return jsonify({'message': f'Hello {current_user["username"]}'})
            return jsonify({'message': 'Hello anonymous'})
    """

    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        current_user = None

        # Check for token in Authorization header
        if "Authorization" in request.headers:
            auth_header = request.headers["Authorization"]
            try:
                token = auth_header.split(" ")[1]
                current_user = decode_access_token(token)
            except (IndexError, AttributeError):
                pass

        return f(current_user, *args, **kwargs)

    return decorated
