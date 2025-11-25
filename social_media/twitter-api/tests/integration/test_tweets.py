"""Integration tests for tweet endpoints."""
from twitter_api.models.user import User
from twitter_api.models.tweet import Tweet


def create_test_user(client, username="testuser", email="test@example.com"):
    """Helper function to create a test user."""
    response = client.post('/api/auth/register', json={
        "username": username,
        "email": email,
        "password": "password123",
        "display_name": f"{username} display"
    })
    return response.get_json()


def login_user(client, username="testuser"):
    """Helper function to login and get token."""
    response = client.post('/api/auth/login', json={
        "username": username,
        "password": "password123"
    })
    return response.get_json()["access_token"]


def test_create_tweet(client, db):
    """Test creating a new tweet."""
    # Create and login user
    user = create_test_user(client)
    token = login_user(client)

    # Create tweet
    response = client.post('/api/tweets', json={
        "content": "This is my first tweet!"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 201
    data = response.get_json()
    assert data["content"] == "This is my first tweet!"
    assert data["user_id"] == user["id"]
    assert data["username"] == "testuser"
    assert "created_at" in data

    # Verify tweet was created in database
    tweet = Tweet.query.filter_by(content="This is my first tweet!").first()
    assert tweet is not None


def test_create_tweet_without_auth(client, db):
    """Test creating a tweet without authentication."""
    response = client.post('/api/tweets', json={
        "content": "This should fail"
    })

    assert response.status_code == 401
    assert "Authentication token is missing" in response.get_json()["error"]


def test_create_tweet_empty_content(client, db):
    """Test creating a tweet with empty content."""
    create_test_user(client)
    token = login_user(client)

    response = client.post('/api/tweets', json={
        "content": ""
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 400
    assert "cannot be empty" in response.get_json()["error"]


def test_create_tweet_content_too_long(client, db):
    """Test creating a tweet with content over 280 characters."""
    create_test_user(client)
    token = login_user(client)

    long_content = "x" * 281
    response = client.post('/api/tweets', json={
        "content": long_content
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 400
    assert "at most 280 characters" in response.get_json()["error"]


def test_create_tweet_exactly_280_chars(client, db):
    """Test creating a tweet with exactly 280 characters."""
    create_test_user(client)
    token = login_user(client)

    content_280 = "x" * 280
    response = client.post('/api/tweets', json={
        "content": content_280
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 201
    data = response.get_json()
    assert len(data["content"]) == 280


def test_create_tweet_whitespace_trimming(client, db):
    """Test that whitespace is trimmed from tweet content."""
    create_test_user(client)
    token = login_user(client)

    response = client.post('/api/tweets', json={
        "content": "  Tweet with spaces  "
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 201
    data = response.get_json()
    assert data["content"] == "Tweet with spaces"


def test_get_all_tweets(client, db):
    """Test getting all tweets."""
    # Create user and tweets
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    for i in range(5):
        tweet = Tweet(content=f"Tweet {i}", user_id=user.id)
        db.session.add(tweet)
    db.session.commit()

    # Get all tweets
    response = client.get('/api/tweets')

    assert response.status_code == 200
    data = response.get_json()
    assert "tweets" in data
    assert "pagination" in data
    assert len(data["tweets"]) == 5


def test_get_all_tweets_pagination(client, db):
    """Test tweet pagination."""
    # Create user and 25 tweets
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    for i in range(25):
        tweet = Tweet(content=f"Tweet {i}", user_id=user.id)
        db.session.add(tweet)
    db.session.commit()

    # Get first page
    response = client.get('/api/tweets?page=1&per_page=10')

    assert response.status_code == 200
    data = response.get_json()
    assert len(data["tweets"]) == 10
    assert data["pagination"]["page"] == 1
    assert data["pagination"]["per_page"] == 10
    assert data["pagination"]["total_items"] == 25
    assert data["pagination"]["total_pages"] == 3

    # Get second page
    response = client.get('/api/tweets?page=2&per_page=10')
    assert response.status_code == 200
    data = response.get_json()
    assert len(data["tweets"]) == 10
    assert data["pagination"]["page"] == 2


def test_get_all_tweets_sort_newest(client, db):
    """Test getting tweets sorted by newest first."""
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    # Create tweets in order
    tweet1 = Tweet(content="First tweet", user_id=user.id)
    db.session.add(tweet1)
    db.session.commit()

    tweet2 = Tweet(content="Second tweet", user_id=user.id)
    db.session.add(tweet2)
    db.session.commit()

    # Get tweets sorted by newest
    response = client.get('/api/tweets?sort=newest')

    assert response.status_code == 200
    data = response.get_json()
    assert data["tweets"][0]["content"] == "Second tweet"
    assert data["tweets"][1]["content"] == "First tweet"


def test_get_all_tweets_sort_oldest(client, db):
    """Test getting tweets sorted by oldest first."""
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    # Create tweets in order
    tweet1 = Tweet(content="First tweet", user_id=user.id)
    db.session.add(tweet1)
    db.session.commit()

    tweet2 = Tweet(content="Second tweet", user_id=user.id)
    db.session.add(tweet2)
    db.session.commit()

    # Get tweets sorted by oldest
    response = client.get('/api/tweets?sort=oldest')

    assert response.status_code == 200
    data = response.get_json()
    assert data["tweets"][0]["content"] == "First tweet"
    assert data["tweets"][1]["content"] == "Second tweet"


def test_get_all_tweets_invalid_sort(client, db):
    """Test getting tweets with invalid sort parameter."""
    response = client.get('/api/tweets?sort=invalid')

    assert response.status_code == 400
    assert "Sort must be 'newest' or 'oldest'" in response.get_json()["error"]


def test_get_all_tweets_invalid_pagination(client, db):
    """Test getting tweets with invalid pagination."""
    # Invalid page
    response = client.get('/api/tweets?page=0')
    assert response.status_code == 400
    assert "Page must be >= 1" in response.get_json()["error"]

    # Invalid per_page
    response = client.get('/api/tweets?per_page=0')
    assert response.status_code == 400
    assert "Per page must be >= 1" in response.get_json()["error"]


def test_get_single_tweet(client, db):
    """Test getting a single tweet by ID."""
    # Create user and tweet
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    tweet = Tweet(content="Test tweet", user_id=user.id)
    db.session.add(tweet)
    db.session.commit()

    # Get tweet
    response = client.get(f'/api/tweets/{tweet.id}')

    assert response.status_code == 200
    data = response.get_json()
    assert data["id"] == tweet.id
    assert data["content"] == "Test tweet"
    assert data["username"] == "testuser"


def test_get_nonexistent_tweet(client, db):
    """Test getting a tweet that doesn't exist."""
    response = client.get('/api/tweets/999')

    assert response.status_code == 404
    assert "Tweet not found" in response.get_json()["error"]


def test_update_tweet(client, db):
    """Test updating a tweet."""
    # Create user and tweet
    user = create_test_user(client)
    token = login_user(client)

    # Create initial tweet
    create_response = client.post('/api/tweets', json={
        "content": "Original content"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    tweet_id = create_response.get_json()["id"]

    # Update tweet
    response = client.put(f'/api/tweets/{tweet_id}', json={
        "content": "Updated content"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 200
    data = response.get_json()
    assert data["content"] == "Updated content"
    assert data["id"] == tweet_id


def test_update_tweet_without_auth(client, db):
    """Test updating a tweet without authentication."""
    # Create user and tweet
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    tweet = Tweet(content="Test tweet", user_id=user.id)
    db.session.add(tweet)
    db.session.commit()

    # Try to update without auth
    response = client.put(f'/api/tweets/{tweet.id}', json={
        "content": "Updated content"
    })

    assert response.status_code == 401
    assert "Authentication token is missing" in response.get_json()["error"]


def test_update_other_user_tweet(client, db):
    """Test updating another user's tweet (should fail)."""
    # Create two users
    user1 = create_test_user(client, "user1", "user1@example.com")
    user2_data = create_test_user(client, "user2", "user2@example.com")
    user2 = User.query.get(user2_data["id"])

    # User2 creates a tweet
    tweet = Tweet(content="User2's tweet", user_id=user2.id)
    db.session.add(tweet)
    db.session.commit()

    # User1 tries to update user2's tweet
    token = login_user(client, "user1")
    response = client.put(f'/api/tweets/{tweet.id}', json={
        "content": "Hacked content"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 403
    assert "You can only edit your own tweets" in response.get_json()["error"]


def test_update_tweet_empty_content(client, db):
    """Test updating a tweet with empty content."""
    create_test_user(client)
    token = login_user(client)

    # Create tweet
    create_response = client.post('/api/tweets', json={
        "content": "Original content"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    tweet_id = create_response.get_json()["id"]

    # Try to update with empty content
    response = client.put(f'/api/tweets/{tweet_id}', json={
        "content": ""
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 400
    assert "cannot be empty" in response.get_json()["error"]


def test_update_nonexistent_tweet(client, db):
    """Test updating a tweet that doesn't exist."""
    create_test_user(client)
    token = login_user(client)

    response = client.put('/api/tweets/999', json={
        "content": "Updated content"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 404
    assert "Tweet not found" in response.get_json()["error"]


def test_delete_tweet(client, db):
    """Test deleting a tweet."""
    # Create user and tweet
    create_test_user(client)
    token = login_user(client)

    # Create tweet
    create_response = client.post('/api/tweets', json={
        "content": "Tweet to delete"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    tweet_id = create_response.get_json()["id"]

    # Delete tweet
    response = client.delete(f'/api/tweets/{tweet_id}', headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 204

    # Verify tweet was deleted
    tweet = Tweet.query.get(tweet_id)
    assert tweet is None


def test_delete_tweet_without_auth(client, db):
    """Test deleting a tweet without authentication."""
    # Create user and tweet
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    tweet = Tweet(content="Test tweet", user_id=user.id)
    db.session.add(tweet)
    db.session.commit()

    # Try to delete without auth
    response = client.delete(f'/api/tweets/{tweet.id}')

    assert response.status_code == 401
    assert "Authentication token is missing" in response.get_json()["error"]


def test_delete_other_user_tweet(client, db):
    """Test deleting another user's tweet (should fail)."""
    # Create two users
    user1 = create_test_user(client, "user1", "user1@example.com")
    user2_data = create_test_user(client, "user2", "user2@example.com")
    user2 = User.query.get(user2_data["id"])

    # User2 creates a tweet
    tweet = Tweet(content="User2's tweet", user_id=user2.id)
    db.session.add(tweet)
    db.session.commit()

    # User1 tries to delete user2's tweet
    token = login_user(client, "user1")
    response = client.delete(f'/api/tweets/{tweet.id}', headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 403
    assert "You can only delete your own tweets" in response.get_json()["error"]

    # Verify tweet still exists
    tweet = Tweet.query.get(tweet.id)
    assert tweet is not None


def test_delete_nonexistent_tweet(client, db):
    """Test deleting a tweet that doesn't exist."""
    create_test_user(client)
    token = login_user(client)

    response = client.delete('/api/tweets/999', headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 404
    assert "Tweet not found" in response.get_json()["error"]
