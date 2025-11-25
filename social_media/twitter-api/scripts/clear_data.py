"""
Clear all data from the database.

This script deletes all tweets and users from the database.
Run from the project root: python scripts/clear_data.py
"""

import sys
import os

# Add parent directory to path to import app modules
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from twitter_api.app import create_app  # noqa: E402
from twitter_api.database import db  # noqa: E402
from twitter_api.models.user import User  # noqa: E402
from twitter_api.models.tweet import Tweet  # noqa: E402
from twitter_api.models.follow import Follow  # noqa: E402


def clear_data():
    """Clear all data from the database."""
    print("=" * 50)
    print("TWITTER API - DATABASE CLEANER")
    print("=" * 50)
    print()

    app = create_app()

    with app.app_context():
        # Get counts before deletion
        tweet_count = Tweet.query.count()
        user_count = User.query.count()
        follow_count = Follow.query.count()

        if tweet_count == 0 and user_count == 0 and follow_count == 0:
            print("Database is already empty. Nothing to clear.")
            return

        print("Current database contents:")
        print(f"  Users: {user_count}")
        print(f"  Tweets: {tweet_count}")
        print(f"  Follows: {follow_count}")
        print()

        response = input(
            "⚠️  Are you sure you want to delete ALL data? "
            "This cannot be undone! (y/N): "
        )

        if response.lower() != "y":
            print("Operation cancelled.")
            return

        print("\nDeleting data...")

        # Delete in order: tweets, follows, users (due to foreign key constraints)
        Tweet.query.delete()
        print(f"  ✓ Deleted {tweet_count} tweets")

        Follow.query.delete()
        print(f"  ✓ Deleted {follow_count} follows")

        # Delete users
        User.query.delete()
        print(f"  ✓ Deleted {user_count} users")

        # Commit the transaction
        db.session.commit()

        print("\n✓ Database cleared successfully!")
        print()


if __name__ == "__main__":
    clear_data()
