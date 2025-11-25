"""Tweet model."""

from datetime import datetime
from twitter_api.database import db


class Tweet(db.Model):
    """Tweet model representing a user's tweet."""

    __tablename__ = "tweets"

    id = db.Column(db.Integer, primary_key=True)
    content = db.Column(db.String(280), nullable=False)
    user_id = db.Column(
        db.Integer, db.ForeignKey("users.id"), nullable=False, index=True
    )
    created_at = db.Column(
        db.DateTime, default=datetime.utcnow, nullable=False, index=True
    )
    updated_at = db.Column(
        db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False
    )

    # Relationships
    user = db.relationship("User", back_populates="tweets")

    def to_dict(self):
        """Convert tweet to dictionary."""
        return {
            "id": self.id,
            "content": self.content,
            "user_id": self.user_id,
            "username": self.user.username if self.user else None,
            "created_at": self.created_at.isoformat() if self.created_at else None,
            "updated_at": self.updated_at.isoformat() if self.updated_at else None,
        }

    def __repr__(self):
        """String representation of Tweet."""
        return f"<Tweet {self.id} by user {self.user_id}>"
