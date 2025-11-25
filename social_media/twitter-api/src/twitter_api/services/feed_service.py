from twitter_api.database import db
from twitter_api.models import Tweet, Follow


class FeedService:
    @staticmethod
    def get_user_feed(user_id, page=1, per_page=20):
        """
        Get personalized feed for a user.
        Shows tweets from users they follow, sorted by newest first.
        """
        per_page = min(per_page, 100)

        # Get IDs of users this user follows
        following_ids = db.session.query(Follow.followed_id).filter(
            Follow.follower_id == user_id
        ).subquery()

        # Get tweets from followed users
        tweets_query = (
            Tweet.query.filter(Tweet.user_id.in_(following_ids))
            .order_by(Tweet.created_at.desc())
        )

        pagination = tweets_query.paginate(
            page=page, per_page=per_page, error_out=False
        )

        return {
            "tweets": [tweet.to_dict() for tweet in pagination.items],
            "pagination": {
                "page": pagination.page,
                "per_page": pagination.per_page,
                "total": pagination.total,
                "pages": pagination.pages,
            },
        }

    @staticmethod
    def get_global_feed(page=1, per_page=20):
        """
        Get global feed of all tweets.
        Useful for discovery or when user follows no one.
        """
        per_page = min(per_page, 100)

        pagination = Tweet.query.order_by(Tweet.created_at.desc()).paginate(
            page=page, per_page=per_page, error_out=False
        )

        return {
            "tweets": [tweet.to_dict() for tweet in pagination.items],
            "pagination": {
                "page": pagination.page,
                "per_page": pagination.per_page,
                "total": pagination.total,
                "pages": pagination.pages,
            },
        }
