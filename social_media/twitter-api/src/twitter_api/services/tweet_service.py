"""Tweet service layer - business logic for tweet operations."""

from typing import Optional, Dict, List, Tuple
from twitter_api.database import db
from twitter_api.models.tweet import Tweet
from twitter_api.models.user import User


class TweetService:
    """Service class for tweet-related operations."""

    @staticmethod
    def validate_content(content: str) -> Tuple[bool, Optional[str]]:
        """
        Validate tweet content.

        Returns:
            Tuple of (is_valid, error_message)
        """
        if not content or not content.strip():
            return False, "Tweet content cannot be empty"
        if len(content) > 280:
            return False, "Tweet content must be at most 280 characters"
        return True, None

    @staticmethod
    def create_tweet(
        user_id: int, content: str
    ) -> Tuple[Optional[Tweet], Optional[str]]:
        """
        Create a new tweet.

        Args:
            user_id: ID of the user creating the tweet
            content: Tweet content

        Returns:
            Tuple of (tweet, error_message)
        """
        # Validate content
        valid, error = TweetService.validate_content(content)
        if not valid:
            return None, error

        # Verify user exists
        user = User.query.get(user_id)
        if not user:
            return None, "User not found"

        # Create tweet
        tweet = Tweet(content=content.strip(), user_id=user_id)

        try:
            db.session.add(tweet)
            db.session.commit()
            return tweet, None
        except Exception as e:
            db.session.rollback()
            return None, f"Error creating tweet: {str(e)}"

    @staticmethod
    def get_tweet_by_id(tweet_id: int) -> Optional[Tweet]:
        """Get a tweet by ID."""
        return Tweet.query.get(tweet_id)

    @staticmethod
    def get_all_tweets(
        page: int = 1, per_page: int = 20, sort: str = "newest"
    ) -> Tuple[List[Tweet], Dict]:
        """
        Get all tweets with pagination.

        Args:
            page: Page number (1-indexed)
            per_page: Number of tweets per page
            sort: Sort order ('newest' or 'oldest')

        Returns:
            Tuple of (tweets_list, pagination_info)
        """
        # Limit per_page to prevent abuse
        per_page = min(per_page, 100)

        # Determine sort order
        if sort == "oldest":
            query = Tweet.query.order_by(Tweet.created_at.asc())
        else:  # default to newest
            query = Tweet.query.order_by(Tweet.created_at.desc())

        pagination = query.paginate(page=page, per_page=per_page, error_out=False)

        pagination_info = {
            "page": pagination.page,
            "per_page": pagination.per_page,
            "total_pages": pagination.pages,
            "total_items": pagination.total,
        }

        return pagination.items, pagination_info

    @staticmethod
    def update_tweet(
        tweet_id: int, user_id: int, content: str
    ) -> Tuple[Optional[Tweet], Optional[str]]:
        """
        Update a tweet.

        Args:
            tweet_id: ID of the tweet to update
            user_id: ID of the user attempting to update
            content: New tweet content

        Returns:
            Tuple of (tweet, error_message)
        """
        # Get tweet
        tweet = Tweet.query.get(tweet_id)
        if not tweet:
            return None, "Tweet not found"

        # Verify ownership
        if tweet.user_id != user_id:
            return None, "You can only edit your own tweets"

        # Validate content
        valid, error = TweetService.validate_content(content)
        if not valid:
            return None, error

        # Update tweet
        tweet.content = content.strip()

        try:
            db.session.commit()
            return tweet, None
        except Exception as e:
            db.session.rollback()
            return None, f"Error updating tweet: {str(e)}"

    @staticmethod
    def delete_tweet(tweet_id: int, user_id: int) -> Tuple[bool, Optional[str]]:
        """
        Delete a tweet.

        Args:
            tweet_id: ID of the tweet to delete
            user_id: ID of the user attempting to delete

        Returns:
            Tuple of (success, error_message)
        """
        # Get tweet
        tweet = Tweet.query.get(tweet_id)
        if not tweet:
            return False, "Tweet not found"

        # Verify ownership
        if tweet.user_id != user_id:
            return False, "You can only delete your own tweets"

        try:
            db.session.delete(tweet)
            db.session.commit()
            return True, None
        except Exception as e:
            db.session.rollback()
            return False, f"Error deleting tweet: {str(e)}"
