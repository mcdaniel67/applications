"""Unit tests for TweetService."""
from twitter_api.services.tweet_service import TweetService


def test_validate_content():
    """Test tweet content validation."""
    # Valid content
    valid, error = TweetService.validate_content("This is a valid tweet")
    assert valid is True
    assert error is None

    # Empty content
    valid, error = TweetService.validate_content("")
    assert valid is False
    assert "cannot be empty" in error

    # Whitespace only
    valid, error = TweetService.validate_content("   ")
    assert valid is False
    assert "cannot be empty" in error

    # Content too long (over 280 characters)
    long_content = "x" * 281
    valid, error = TweetService.validate_content(long_content)
    assert valid is False
    assert "at most 280 characters" in error

    # Exactly 280 characters (should be valid)
    valid, error = TweetService.validate_content("x" * 280)
    assert valid is True
