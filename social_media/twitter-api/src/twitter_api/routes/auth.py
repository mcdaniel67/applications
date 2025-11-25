"""Authentication routes."""

from flask import Blueprint, jsonify, request
from twitter_api.services.user_service import UserService
from twitter_api.utils.decorators import token_required

bp = Blueprint("auth", __name__, url_prefix="/api/auth")


@bp.route("/register", methods=["POST"])
def register():
    """Register a new user.
    ---
    tags:
      - Authentication
    parameters:
      - in: body
        name: body
        required: true
        schema:
          type: object
          required:
            - username
            - email
            - password
          properties:
            username:
              type: string
              minLength: 3
              maxLength: 50
              example: johndoe
              description: Alphanumeric username with underscores allowed
            email:
              type: string
              format: email
              example: john@example.com
            password:
              type: string
              minLength: 8
              maxLength: 128
              example: password123
            display_name:
              type: string
              maxLength: 100
              example: John Doe
              description: Optional display name
    responses:
      201:
        description: User successfully registered
        schema:
          type: object
          properties:
            id:
              type: integer
            username:
              type: string
            email:
              type: string
            display_name:
              type: string
            bio:
              type: string
            created_at:
              type: string
              format: date-time
      400:
        description: Validation error or duplicate username/email
        schema:
          type: object
          properties:
            error:
              type: string
              example: Username already exists
    """
    data = request.get_json()

    # Validate required fields
    if not data:
        return jsonify({"error": "No data provided"}), 400

    username = data.get("username")
    email = data.get("email")
    password = data.get("password")
    display_name = data.get("display_name")

    if not username:
        return jsonify({"error": "Username is required"}), 400
    if not email:
        return jsonify({"error": "Email is required"}), 400
    if not password:
        return jsonify({"error": "Password is required"}), 400

    # Register user
    user, error = UserService.register_user(
        username=username, email=email, password=password, display_name=display_name
    )

    if error:
        return jsonify({"error": error}), 400

    return jsonify(user.to_dict()), 201


@bp.route("/login", methods=["POST"])
def login():
    """Login a user and receive JWT token.
    ---
    tags:
      - Authentication
    parameters:
      - in: body
        name: body
        required: true
        schema:
          type: object
          required:
            - username
            - password
          properties:
            username:
              type: string
              example: johndoe
            password:
              type: string
              example: password123
    responses:
      200:
        description: Successfully authenticated
        schema:
          type: object
          properties:
            access_token:
              type: string
              description: JWT access token
            token_type:
              type: string
              example: Bearer
            user:
              type: object
              properties:
                id:
                  type: integer
                username:
                  type: string
                email:
                  type: string
                display_name:
                  type: string
      401:
        description: Invalid credentials
        schema:
          type: object
          properties:
            error:
              type: string
              example: Invalid username or password
    """
    data = request.get_json()

    # Validate required fields
    if not data:
        return jsonify({"error": "No data provided"}), 400

    username = data.get("username")
    password = data.get("password")

    if not username:
        return jsonify({"error": "Username is required"}), 400
    if not password:
        return jsonify({"error": "Password is required"}), 400

    # Authenticate user
    token, user, error = UserService.authenticate_user(username, password)

    if error:
        return jsonify({"error": error}), 401

    return (
        jsonify(
            {"access_token": token, "token_type": "Bearer", "user": user.to_dict()}
        ),
        200,
    )


@bp.route("/logout", methods=["POST"])
@token_required
def logout(current_user):
    """Logout a user.
    ---
    tags:
      - Authentication
    security:
      - Bearer: []
    responses:
      200:
        description: Successfully logged out
        schema:
          type: object
          properties:
            message:
              type: string
              example: Successfully logged out
      401:
        description: Invalid or missing token
        schema:
          type: object
          properties:
            error:
              type: string
              example: Token is invalid or expired
    """
    return jsonify({"message": "Successfully logged out"}), 200
