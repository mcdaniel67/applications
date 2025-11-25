"""Flask application factory."""

import os
from flask import Flask, jsonify
from flask_cors import CORS
from flasgger import Swagger

from twitter_api.config import config
from twitter_api.database import init_db


def create_app(config_name=None):
    """Create and configure the Flask application."""
    if config_name is None:
        config_name = os.getenv("FLASK_ENV", "development")

    app = Flask(__name__)
    app.config.from_object(config.get(config_name, config["default"]))

    # Initialize extensions
    CORS(app)
    init_db(app)

    # Configure Swagger/OpenAPI documentation
    swagger_config = {
        "headers": [],
        "specs": [
            {
                "endpoint": "apispec",
                "route": "/apispec.json",
                "rule_filter": lambda rule: True,
                "model_filter": lambda tag: True,
            }
        ],
        "static_url_path": "/flasgger_static",
        "swagger_ui": True,
        "specs_route": "/apidocs/",
    }

    swagger_template = {
        "swagger": "2.0",
        "info": {
            "title": "Twitter Clone API",
            "description": "A fully functional Twitter clone REST API with authentication, tweets, follows, and feeds",
            "version": "0.1.0",
            "contact": {
                "name": "API Support",
            },
        },
        "host": "localhost:5000",
        "basePath": "/",
        "schemes": ["http"],
        "securityDefinitions": {
            "Bearer": {
                "type": "apiKey",
                "name": "Authorization",
                "in": "header",
                "description": "JWT Authorization header using the Bearer scheme. Example: 'Authorization: Bearer {token}'",
            }
        },
        "tags": [
            {"name": "Authentication", "description": "User registration and login"},
            {"name": "Users", "description": "User profile management"},
            {"name": "Tweets", "description": "Tweet CRUD operations"},
            {"name": "Follows", "description": "Follow and unfollow users"},
            {"name": "Feed", "description": "Timeline and feed operations"},
            {"name": "Health", "description": "API health check"},
        ],
    }

    Swagger(app, config=swagger_config, template=swagger_template)

    # Register blueprints
    from twitter_api.routes import auth, tweets, users, follows, feed

    app.register_blueprint(auth.bp)
    app.register_blueprint(tweets.bp)
    app.register_blueprint(users.bp)
    app.register_blueprint(follows.follows_bp)
    app.register_blueprint(feed.feed_bp)

    # Health check endpoint
    @app.route("/health")
    def health_check():
        """Health check endpoint.
        ---
        tags:
          - Health
        responses:
          200:
            description: API is healthy and running
            schema:
              type: object
              properties:
                status:
                  type: string
                  example: healthy
                message:
                  type: string
                  example: Twitter API is running
        """
        return jsonify({"status": "healthy", "message": "Twitter API is running"})

    # Root endpoint
    @app.route("/")
    def index():
        """Root endpoint."""
        return jsonify(
            {
                "message": "Welcome to Twitter API",
                "version": "0.1.0",
                "endpoints": {
                    "health": "/health",
                    "auth": "/api/auth",
                    "tweets": "/api/tweets",
                    "users": "/api/users",
                },
            }
        )

    return app
