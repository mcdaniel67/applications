"""Unit tests for UserService."""
from twitter_api.services.user_service import UserService


def test_validate_email():
    """Test email validation."""
    # Valid emails
    valid, error = UserService.validate_email("test@example.com")
    assert valid is True
    assert error is None

    valid, error = UserService.validate_email("user.name+tag@example.co.uk")
    assert valid is True

    # Invalid emails
    valid, error = UserService.validate_email("invalid")
    assert valid is False
    assert "Invalid email format" in error

    valid, error = UserService.validate_email("@example.com")
    assert valid is False

    valid, error = UserService.validate_email("user@")
    assert valid is False


def test_validate_username():
    """Test username validation."""
    # Valid usernames
    valid, error = UserService.validate_username("validuser")
    assert valid is True
    assert error is None

    valid, error = UserService.validate_username("user_123")
    assert valid is True

    # Too short
    valid, error = UserService.validate_username("ab")
    assert valid is False
    assert "at least 3 characters" in error

    # Too long
    valid, error = UserService.validate_username("a" * 51)
    assert valid is False
    assert "at most 50 characters" in error

    # Invalid characters
    valid, error = UserService.validate_username("user-name")
    assert valid is False
    assert "letters, numbers, and underscores" in error

    valid, error = UserService.validate_username("user name")
    assert valid is False


def test_validate_password():
    """Test password validation."""
    # Valid passwords
    valid, error = UserService.validate_password("password123")
    assert valid is True
    assert error is None

    valid, error = UserService.validate_password("SecurePass123!")
    assert valid is True

    # Too short
    valid, error = UserService.validate_password("short")
    assert valid is False
    assert "at least 8 characters" in error

    # Too long
    valid, error = UserService.validate_password("a" * 129)
    assert valid is False
    assert "at most 128 characters" in error
