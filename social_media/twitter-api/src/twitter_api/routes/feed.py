from flask import Blueprint, jsonify, request
from twitter_api.services.feed_service import FeedService
from twitter_api.utils.decorators import token_required

feed_bp = Blueprint("feed", __name__, url_prefix="/api")


@feed_bp.route("/feed", methods=["GET"])
@token_required
def get_feed(current_user):
    """Get personalized feed for authenticated user.
    ---
    tags:
      - Feed
    security:
      - Bearer: []
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
        description: Number of tweets per page
    responses:
      200:
        description: Personalized timeline showing tweets from followed users
        schema:
          type: object
          properties:
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
      401:
        description: Unauthorized
        schema:
          type: object
          properties:
            error:
              type: string
    """
    page = request.args.get("page", 1, type=int)
    per_page = request.args.get("per_page", 20, type=int)

    if page < 1:
        return jsonify({"error": "Page must be >= 1"}), 400
    if per_page < 1:
        return jsonify({"error": "Per page must be >= 1"}), 400

    result = FeedService.get_user_feed(current_user["user_id"], page, per_page)
    return jsonify(result), 200


@feed_bp.route("/feed/global", methods=["GET"])
def get_global_feed():
    """Get global feed of all tweets (public endpoint).
    ---
    tags:
      - Feed
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
        description: Number of tweets per page
    responses:
      200:
        description: Global timeline showing all tweets from all users
        schema:
          type: object
          properties:
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
    page = request.args.get("page", 1, type=int)
    per_page = request.args.get("per_page", 20, type=int)

    if page < 1:
        return jsonify({"error": "Page must be >= 1"}), 400
    if per_page < 1:
        return jsonify({"error": "Per page must be >= 1"}), 400

    result = FeedService.get_global_feed(page, per_page)
    return jsonify(result), 200
