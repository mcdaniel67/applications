from datetime import datetime
from twitter_api.database import db


class Follow(db.Model):
    __tablename__ = "follows"

    follower_id = db.Column(db.Integer, db.ForeignKey("users.id"), primary_key=True)
    followed_id = db.Column(db.Integer, db.ForeignKey("users.id"), primary_key=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    def to_dict(self):
        return {
            "follower_id": self.follower_id,
            "followed_id": self.followed_id,
            "created_at": self.created_at.isoformat(),
        }
