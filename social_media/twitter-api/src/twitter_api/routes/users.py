"""User routes."""

from flask import Blueprint, jsonify, request
from twitter_api.services.user_service import UserService
from twitter_api.models.tweet import Tweet
from twitter_api.utils.decorators import token_required

bp = Blueprint("users", __name__, url_prefix="/api/users")


@bp.route("", methods=["GET"])
def get_users():
    """Get all users with pagination.
    ---
    tags:
      - Users
    parameters:
      - name: page
        in: query
        type: integer
        default: 1
        minimum: 1
        description: Page number
      - name: per_page
        in: query
        type: integer
        default: 20
        maximum: 100
        description: Number of users per page
    responses:
      200:
        description: List of users with pagination info
        schema:
          type: object
          properties:
            users:
              type: array
              items:
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
            pagination:
              type: object
              properties:
                page:
                  type: integer
                per_page:
                  type: integer
                total:
                  type: integer
                pages:
                  type: integer
      400:
        description: Invalid parameters
        schema:
          type: object
          properties:
            error:
              type: string
    """
    # Get pagination parameters from query string
    page = request.args.get("page", 1, type=int)
    per_page = request.args.get("per_page", 20, type=int)

    # Validate pagination parameters
    if page < 1:
        return jsonify({"error": "Page must be >= 1"}), 400
    if per_page < 1:
        return jsonify({"error": "Per page must be >= 1"}), 400

    # Get users
    users, pagination_info = UserService.get_all_users(page, per_page)

    return (
        jsonify(
            {"users": [user.to_dict() for user in users], "pagination": pagination_info}
        ),
        200,
    )


@bp.route("/<int:user_id>", methods=["GET"])
def get_user(user_id):
    """Get a specific user by ID.
    ---
    tags:
      - Users
    parameters:
      - name: user_id
        in: path
        type: integer
        required: true
        description: User ID
    responses:
      200:
        description: User profile with stats
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
            tweet_count:
              type: integer
              description: Number of tweets by this user
            followers_count:
              type: integer
              description: Number of followers
            following_count:
              type: integer
              description: Number of users being followed
      404:
        description: User not found
        schema:
          type: object
          properties:
            error:
              type: string
              example: User not found
    """
    from twitter_api.services.follow_service import FollowService

    user = UserService.get_user_by_id(user_id)

    if not user:
        return jsonify({"error": "User not found"}), 404

    # Get additional stats
    tweet_count = Tweet.query.filter_by(user_id=user_id).count()
    follow_counts = FollowService.get_follow_counts(user_id)

    user_data = user.to_dict()
    user_data["tweet_count"] = tweet_count
    user_data["followers_count"] = follow_counts["followers_count"]
    user_data["following_count"] = follow_counts["following_count"]

    return jsonify(user_data), 200


@bp.route("/<int:user_id>", methods=["PUT"])
@token_required
def update_user(current_user, user_id):
    """Update a user profile.
    ---
    tags:
      - Users
    security:
      - Bearer: []
    parameters:
      - name: user_id
        in: path
        type: integer
        required: true
        description: User ID
      - in: body
        name: body
        schema:
          type: object
          properties:
            display_name:
              type: string
              maxLength: 100
              example: John Doe
              description: Display name (optional)
            bio:
              type: string
              maxLength: 500
              example: Software developer and coffee enthusiast
              description: User bio (optional)
    responses:
      200:
        description: User profile updated successfully
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
        description: Validation error
        schema:
          type: object
          properties:
            error:
              type: string
      401:
        description: Unauthorized
        schema:
          type: object
          properties:
            error:
              type: string
      403:
        description: Forbidden - can only update own profile
        schema:
          type: object
          properties:
            error:
              type: string
              example: You can only update your own profile
    """
    # Check if user is updating their own profile
    if current_user["user_id"] != user_id:
        return jsonify({"error": "You can only update your own profile"}), 403

    data = request.get_json()
    if not data:
        return jsonify({"error": "No data provided"}), 400

    display_name = data.get("display_name")
    bio = data.get("bio")

    # Update user
    user, error = UserService.update_user(
        user_id=user_id, display_name=display_name, bio=bio
    )

    if error:
        return jsonify({"error": error}), 400

    return jsonify(user.to_dict()), 200


@bp.route("/<int:user_id>/tweets", methods=["GET"])
def get_user_tweets(user_id):
    """Get all tweets by a specific user with pagination.
    ---
    tags:
      - Users
    parameters:
      - name: user_id
        in: path
        type: integer
        required: true
        description: User ID
      - name: page
        in: query
        type: integer
        default: 1
        minimum: 1
        description: Page number
      - name: per_page
        in: query
        type: integer
        default: 20
        maximum: 100
        description: Number of tweets per page
    responses:
      200:
        description: User's tweets with pagination
        schema:
          type: object
          properties:
            user:
              type: object
              properties:
                id:
                  type: integer
                username:
                  type: string
                display_name:
                  type: string
            tweets:
              type: array
              items:
                type: object
                properties:
                  id:
                    type: integer
                  user_id:
                    type: integer
                  username:
                    type: string
                  content:
                    type: string
                  created_at:
                    type: string
                    format: date-time
            pagination:
              type: object
              properties:
                page:
                  type: integer
                per_page:
                  type: integer
                total_pages:
                  type: integer
                total_items:
                  type: integer
      400:
        description: Invalid parameters
        schema:
          type: object
          properties:
            error:
              type: string
      404:
        description: User not found
        schema:
          type: object
          properties:
            error:
              type: string
    """
    # Check if user exists
    user = UserService.get_user_by_id(user_id)
    if not user:
        return jsonify({"error": "User not found"}), 404

    # Get pagination parameters
    page = request.args.get("page", 1, type=int)
    per_page = request.args.get("per_page", 20, type=int)

    # Validate pagination parameters
    if page < 1:
        return jsonify({"error": "Page must be >= 1"}), 400
    if per_page < 1:
        return jsonify({"error": "Per page must be >= 1"}), 400

    # Limit per_page
    per_page = min(per_page, 100)

    # Get tweets
    pagination = (
        Tweet.query.filter_by(user_id=user_id)
        .order_by(Tweet.created_at.desc())
        .paginate(page=page, per_page=per_page, error_out=False)
    )

    return (
        jsonify(
            {
                "user": {
                    "id": user.id,
                    "username": user.username,
                    "display_name": user.display_name,
                },
                "tweets": [tweet.to_dict() for tweet in pagination.items],
                "pagination": {
                    "page": pagination.page,
                    "per_page": pagination.per_page,
                    "total_pages": pagination.pages,
                    "total_items": pagination.total,
                },
            }
        ),
        200,
    )
