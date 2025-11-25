"""Tweet routes."""

from flask import Blueprint, jsonify, request
from twitter_api.services.tweet_service import TweetService
from twitter_api.utils.decorators import token_required

bp = Blueprint("tweets", __name__, url_prefix="/api/tweets")


@bp.route("", methods=["GET"])
def get_tweets():
    """Get all tweets with pagination.
    ---
    tags:
      - Tweets
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
      - name: sort
        in: query
        type: string
        enum: [newest, oldest]
        default: newest
        description: Sort order for tweets
    responses:
      200:
        description: List of tweets with pagination info
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
                  updated_at:
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
    sort = request.args.get("sort", "newest", type=str)

    # Validate pagination parameters
    if page < 1:
        return jsonify({"error": "Page must be >= 1"}), 400
    if per_page < 1:
        return jsonify({"error": "Per page must be >= 1"}), 400

    # Validate sort parameter
    if sort not in ["newest", "oldest"]:
        return jsonify({"error": "Sort must be 'newest' or 'oldest'"}), 400

    # Get tweets
    tweets, pagination_info = TweetService.get_all_tweets(page, per_page, sort)

    return (
        jsonify(
            {
                "tweets": [tweet.to_dict() for tweet in tweets],
                "pagination": pagination_info,
            }
        ),
        200,
    )


@bp.route("/<int:tweet_id>", methods=["GET"])
def get_tweet(tweet_id):
    """Get a specific tweet by ID.
    ---
    tags:
      - Tweets
    parameters:
      - name: tweet_id
        in: path
        type: integer
        required: true
        description: Tweet ID
    responses:
      200:
        description: Tweet details
        schema:
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
            updated_at:
              type: string
              format: date-time
      404:
        description: Tweet not found
        schema:
          type: object
          properties:
            error:
              type: string
              example: Tweet not found
    """
    tweet = TweetService.get_tweet_by_id(tweet_id)

    if not tweet:
        return jsonify({"error": "Tweet not found"}), 404

    return jsonify(tweet.to_dict()), 200


@bp.route("", methods=["POST"])
@token_required
def create_tweet(current_user):
    """Create a new tweet.
    ---
    tags:
      - Tweets
    security:
      - Bearer: []
    parameters:
      - in: body
        name: body
        required: true
        schema:
          type: object
          required:
            - content
          properties:
            content:
              type: string
              minLength: 1
              maxLength: 280
              example: Hello Twitter!
    responses:
      201:
        description: Tweet created successfully
        schema:
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
    """
    data = request.get_json()

    # Validate required fields
    if not data:
        return jsonify({"error": "No data provided"}), 400

    content = data.get("content")

    if content is None:
        return jsonify({"error": "Content is required"}), 400

    # Create tweet
    tweet, error = TweetService.create_tweet(
        user_id=current_user["user_id"], content=content
    )

    if error:
        return jsonify({"error": error}), 400

    return jsonify(tweet.to_dict()), 201


@bp.route("/<int:tweet_id>", methods=["PUT"])
@token_required
def update_tweet(current_user, tweet_id):
    """Update a tweet.
    ---
    tags:
      - Tweets
    security:
      - Bearer: []
    parameters:
      - name: tweet_id
        in: path
        type: integer
        required: true
        description: Tweet ID
      - in: body
        name: body
        required: true
        schema:
          type: object
          required:
            - content
          properties:
            content:
              type: string
              minLength: 1
              maxLength: 280
              example: Updated tweet content
    responses:
      200:
        description: Tweet updated successfully
        schema:
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
            updated_at:
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
        description: Forbidden - can only edit own tweets
        schema:
          type: object
          properties:
            error:
              type: string
              example: You can only edit your own tweets
      404:
        description: Tweet not found
        schema:
          type: object
          properties:
            error:
              type: string
    """
    data = request.get_json()

    # Validate required fields
    if not data:
        return jsonify({"error": "No data provided"}), 400

    content = data.get("content")

    if content is None:
        return jsonify({"error": "Content is required"}), 400

    # Update tweet
    tweet, error = TweetService.update_tweet(
        tweet_id=tweet_id, user_id=current_user["user_id"], content=content
    )

    if error:
        if error == "Tweet not found":
            return jsonify({"error": error}), 404
        elif error == "You can only edit your own tweets":
            return jsonify({"error": error}), 403
        else:
            return jsonify({"error": error}), 400

    return jsonify(tweet.to_dict()), 200


@bp.route("/<int:tweet_id>", methods=["DELETE"])
@token_required
def delete_tweet(current_user, tweet_id):
    """Delete a tweet.
    ---
    tags:
      - Tweets
    security:
      - Bearer: []
    parameters:
      - name: tweet_id
        in: path
        type: integer
        required: true
        description: Tweet ID
    responses:
      204:
        description: Tweet deleted successfully
      401:
        description: Unauthorized
        schema:
          type: object
          properties:
            error:
              type: string
      403:
        description: Forbidden - can only delete own tweets
        schema:
          type: object
          properties:
            error:
              type: string
              example: You can only delete your own tweets
      404:
        description: Tweet not found
        schema:
          type: object
          properties:
            error:
              type: string
    """
    # Delete tweet
    success, error = TweetService.delete_tweet(
        tweet_id=tweet_id, user_id=current_user["user_id"]
    )

    if error:
        if error == "Tweet not found":
            return jsonify({"error": error}), 404
        elif error == "You can only delete your own tweets":
            return jsonify({"error": error}), 403
        else:
            return jsonify({"error": error}), 400

    return "", 204
