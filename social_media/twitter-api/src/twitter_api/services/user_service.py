"""User service layer - business logic for user operations."""

from typing import Optional, Dict, List, Tuple
from twitter_api.database import db
from twitter_api.models.user import User
from twitter_api.utils.password import hash_password, verify_password
from twitter_api.utils.jwt import create_access_token
import re


class UserService:
    """Service class for user-related operations."""

    @staticmethod
    def validate_email(email: str) -> Tuple[bool, Optional[str]]:
        """
        Validate email format.

        Returns:
            Tuple of (is_valid, error_message)
        """
        email_regex = r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"
        if not re.match(email_regex, email):
            return False, "Invalid email format"
        return True, None

    @staticmethod
    def validate_username(username: str) -> Tuple[bool, Optional[str]]:
        """
        Validate username format.

        Returns:
            Tuple of (is_valid, error_message)
        """
        if len(username) < 3:
            return False, "Username must be at least 3 characters"
        if len(username) > 50:
            return False, "Username must be at most 50 characters"
        if not re.match(r"^[a-zA-Z0-9_]+$", username):
            return False, "Username can only contain letters, numbers, and underscores"
        return True, None

    @staticmethod
    def validate_password(password: str) -> Tuple[bool, Optional[str]]:
        """
        Validate password strength.

        Returns:
            Tuple of (is_valid, error_message)
        """
        if len(password) < 8:
            return False, "Password must be at least 8 characters"
        if len(password) > 128:
            return False, "Password must be at most 128 characters"
        return True, None

    @staticmethod
    def register_user(
        username: str,
        email: str,
        password: str,
        display_name: Optional[str] = None,
    ) -> Tuple[Optional[User], Optional[str]]:
        """
        Register a new user.

        Args:
            username: User's username
            email: User's email
            password: User's plain text password
            display_name: Optional display name

        Returns:
            Tuple of (user, error_message)
        """
        # Validate username
        valid, error = UserService.validate_username(username)
        if not valid:
            return None, error

        # Validate email
        valid, error = UserService.validate_email(email)
        if not valid:
            return None, error

        # Validate password
        valid, error = UserService.validate_password(password)
        if not valid:
            return None, error

        # Check if username already exists
        if User.query.filter_by(username=username).first():
            return None, "Username already exists"

        # Check if email already exists
        if User.query.filter_by(email=email).first():
            return None, "Email already exists"

        # Validate display name length if provided
        if display_name and len(display_name) > 100:
            return None, "Display name must be at most 100 characters"

        # Create user
        user = User(
            username=username,
            email=email,
            password_hash=hash_password(password),
            display_name=display_name,
        )

        try:
            db.session.add(user)
            db.session.commit()
            return user, None
        except Exception as e:
            db.session.rollback()
            return None, f"Error creating user: {str(e)}"

    @staticmethod
    def authenticate_user(
        username: str, password: str
    ) -> Tuple[Optional[str], Optional[User], Optional[str]]:
        """
        Authenticate a user and generate access token.

        Args:
            username: User's username
            password: User's plain text password

        Returns:
            Tuple of (token, user, error_message)
        """
        # Find user by username
        user = User.query.filter_by(username=username).first()

        if not user:
            return None, None, "Invalid username or password"

        # Verify password
        if not verify_password(password, user.password_hash):
            return None, None, "Invalid username or password"

        # Generate token
        token = create_access_token(user.id, user.username)
        return token, user, None

    @staticmethod
    def get_user_by_id(user_id: int) -> Optional[User]:
        """Get a user by ID."""
        return User.query.get(user_id)

    @staticmethod
    def get_user_by_username(username: str) -> Optional[User]:
        """Get a user by username."""
        return User.query.filter_by(username=username).first()

    @staticmethod
    def get_all_users(page: int = 1, per_page: int = 20) -> Tuple[List[User], Dict]:
        """
        Get all users with pagination.

        Args:
            page: Page number (1-indexed)
            per_page: Number of users per page

        Returns:
            Tuple of (users_list, pagination_info)
        """
        # Limit per_page to prevent abuse
        per_page = min(per_page, 100)

        pagination = User.query.order_by(User.created_at.desc()).paginate(
            page=page, per_page=per_page, error_out=False
        )

        pagination_info = {
            "page": pagination.page,
            "per_page": pagination.per_page,
            "total_pages": pagination.pages,
            "total_items": pagination.total,
        }

        return pagination.items, pagination_info

    @staticmethod
    def update_user(
        user_id: int,
        display_name: Optional[str] = None,
        bio: Optional[str] = None,
    ) -> Tuple[Optional[User], Optional[str]]:
        """
        Update user profile.

        Args:
            user_id: User's ID
            display_name: New display name (optional)
            bio: New bio (optional)

        Returns:
            Tuple of (user, error_message)
        """
        user = User.query.get(user_id)
        if not user:
            return None, "User not found"

        # Update fields if provided
        if display_name is not None:
            if len(display_name) > 100:
                return None, "Display name must be at most 100 characters"
            user.display_name = display_name

        if bio is not None:
            if len(bio) > 500:
                return None, "Bio must be at most 500 characters"
            user.bio = bio

        try:
            db.session.commit()
            return user, None
        except Exception as e:
            db.session.rollback()
            return None, f"Error updating user: {str(e)}"
