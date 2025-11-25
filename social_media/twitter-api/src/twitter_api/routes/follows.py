from flask import Blueprint, jsonify, request
from twitter_api.services.follow_service import FollowService
from twitter_api.utils.decorators import token_required

follows_bp = Blueprint("follows", __name__, url_prefix="/api")


@follows_bp.route("/users/<int:user_id>/follow", methods=["POST"])
@token_required
def follow_user(current_user, user_id):
    """Follow a user.
    ---
    tags:
      - Follows
    security:
      - Bearer: []
    parameters:
      - name: user_id
        in: path
        type: integer
        required: true
        description: ID of user to follow
    responses:
      201:
        description: Successfully followed user
        schema:
          type: object
          properties:
            message:
              type: string
              example: Successfully followed user
      400:
        description: Cannot follow (self-follow or already following)
        schema:
          type: object
          properties:
            error:
              type: string
              example: Cannot follow yourself
      401:
        description: Unauthorized
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
    follow, error = FollowService.follow_user(current_user["user_id"], user_id)

    if error:
        status_code = 400 if "Cannot" in error or "Already" in error else 404
        return jsonify({"error": error}), status_code

    return jsonify({"message": "Successfully followed user"}), 201


@follows_bp.route("/users/<int:user_id>/follow", methods=["DELETE"])
@token_required
def unfollow_user(current_user, user_id):
    """Unfollow a user.
    ---
    tags:
      - Follows
    security:
      - Bearer: []
    parameters:
      - name: user_id
        in: path
        type: integer
        required: true
        description: ID of user to unfollow
    responses:
      204:
        description: Successfully unfollowed user
      401:
        description: Unauthorized
        schema:
          type: object
          properties:
            error:
              type: string
      404:
        description: Not following this user
        schema:
          type: object
          properties:
            error:
              type: string
              example: You are not following this user
    """
    success, error = FollowService.unfollow_user(current_user["user_id"], user_id)

    if error:
        return jsonify({"error": error}), 404

    return "", 204


@follows_bp.route("/users/<int:user_id>/followers", methods=["GET"])
def get_followers(user_id):
    """Get a user's followers.
    ---
    tags:
      - Follows
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
        description: Number of followers per page
    responses:
      200:
        description: List of followers
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
                  display_name:
                    type: string
                  bio:
                    type: string
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
    """
    page = request.args.get("page", 1, type=int)
    per_page = request.args.get("per_page", 20, type=int)

    result = FollowService.get_followers(user_id, page, per_page)
    return jsonify(result), 200


@follows_bp.route("/users/<int:user_id>/following", methods=["GET"])
def get_following(user_id):
    """Get users this user is following.
    ---
    tags:
      - Follows
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
        description: Number of users per page
    responses:
      200:
        description: List of users being followed
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
                  display_name:
                    type: string
                  bio:
                    type: string
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
    """
    page = request.args.get("page", 1, type=int)
    per_page = request.args.get("per_page", 20, type=int)

    result = FollowService.get_following(user_id, page, per_page)
    return jsonify(result), 200
