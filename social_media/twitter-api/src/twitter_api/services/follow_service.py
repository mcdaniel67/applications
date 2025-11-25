from twitter_api.database import db
from twitter_api.models import User, Follow


class FollowService:
    @staticmethod
    def follow_user(follower_id, followed_id):
        """Follow a user."""
        if follower_id == followed_id:
            return None, "Cannot follow yourself"

        follower = User.query.get(follower_id)
        followed = User.query.get(followed_id)

        if not follower or not followed:
            return None, "User not found"

        existing = Follow.query.filter_by(
            follower_id=follower_id, followed_id=followed_id
        ).first()

        if existing:
            return None, "Already following this user"

        follow = Follow(follower_id=follower_id, followed_id=followed_id)
        db.session.add(follow)
        db.session.commit()

        return follow, None

    @staticmethod
    def unfollow_user(follower_id, followed_id):
        """Unfollow a user."""
        follow = Follow.query.filter_by(
            follower_id=follower_id, followed_id=followed_id
        ).first()

        if not follow:
            return False, "Not following this user"

        db.session.delete(follow)
        db.session.commit()
        return True, None

    @staticmethod
    def get_followers(user_id, page=1, per_page=20):
        """Get users following this user."""
        per_page = min(per_page, 100)

        follows = (
            Follow.query.filter_by(followed_id=user_id)
            .order_by(Follow.created_at.desc())
            .paginate(page=page, per_page=per_page, error_out=False)
        )

        follower_ids = [f.follower_id for f in follows.items]
        users = User.query.filter(User.id.in_(follower_ids)).all()

        return {
            "users": [u.to_dict() for u in users],
            "pagination": {
                "page": follows.page,
                "per_page": follows.per_page,
                "total": follows.total,
                "pages": follows.pages,
            },
        }

    @staticmethod
    def get_following(user_id, page=1, per_page=20):
        """Get users this user is following."""
        per_page = min(per_page, 100)

        follows = (
            Follow.query.filter_by(follower_id=user_id)
            .order_by(Follow.created_at.desc())
            .paginate(page=page, per_page=per_page, error_out=False)
        )

        followed_ids = [f.followed_id for f in follows.items]
        users = User.query.filter(User.id.in_(followed_ids)).all()

        return {
            "users": [u.to_dict() for u in users],
            "pagination": {
                "page": follows.page,
                "per_page": follows.per_page,
                "total": follows.total,
                "pages": follows.pages,
            },
        }

    @staticmethod
    def is_following(follower_id, followed_id):
        """Check if follower_id is following followed_id."""
        return (
            Follow.query.filter_by(
                follower_id=follower_id, followed_id=followed_id
            ).first()
            is not None
        )

    @staticmethod
    def get_follow_counts(user_id):
        """Get follower and following counts for a user."""
        followers_count = Follow.query.filter_by(followed_id=user_id).count()
        following_count = Follow.query.filter_by(follower_id=user_id).count()

        return {
            "followers_count": followers_count,
            "following_count": following_count,
        }
