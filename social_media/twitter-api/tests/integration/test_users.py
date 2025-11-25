"""Integration tests for user endpoints."""
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


def test_get_all_users(client, db):
    """Test getting all users with pagination."""
    # Create multiple users
    for i in range(5):
        create_test_user(client, f"user{i}", f"user{i}@example.com")

    # Get all users
    response = client.get('/api/users')

    assert response.status_code == 200
    data = response.get_json()
    assert "users" in data
    assert "pagination" in data
    assert len(data["users"]) == 5
    assert data["pagination"]["total_items"] == 5


def test_get_all_users_pagination(client, db):
    """Test user pagination."""
    # Create 25 users
    for i in range(25):
        create_test_user(client, f"user{i}", f"user{i}@example.com")

    # Get first page (default 20 per page)
    response = client.get('/api/users?page=1&per_page=10')

    assert response.status_code == 200
    data = response.get_json()
    assert len(data["users"]) == 10
    assert data["pagination"]["page"] == 1
    assert data["pagination"]["per_page"] == 10
    assert data["pagination"]["total_items"] == 25
    assert data["pagination"]["total_pages"] == 3

    # Get second page
    response = client.get('/api/users?page=2&per_page=10')
    assert response.status_code == 200
    data = response.get_json()
    assert len(data["users"]) == 10
    assert data["pagination"]["page"] == 2


def test_get_all_users_invalid_pagination(client, db):
    """Test user pagination with invalid parameters."""
    # Invalid page
    response = client.get('/api/users?page=0')
    assert response.status_code == 400
    assert "Page must be >= 1" in response.get_json()["error"]

    # Invalid per_page
    response = client.get('/api/users?per_page=0')
    assert response.status_code == 400
    assert "Per page must be >= 1" in response.get_json()["error"]


def test_get_single_user(client, db):
    """Test getting a single user by ID."""
    # Create user
    user = create_test_user(client)

    # Get user
    response = client.get(f'/api/users/{user["id"]}')

    assert response.status_code == 200
    data = response.get_json()
    assert data["id"] == user["id"]
    assert data["username"] == "testuser"
    assert data["display_name"] == "testuser display"
    assert "tweet_count" in data
    assert data["tweet_count"] == 0


def test_get_nonexistent_user(client, db):
    """Test getting a user that doesn't exist."""
    response = client.get('/api/users/999')

    assert response.status_code == 404
    assert "User not found" in response.get_json()["error"]


def test_update_user_profile(client, db):
    """Test updating user profile."""
    # Create and login user
    user = create_test_user(client)
    token = login_user(client)

    # Update profile
    response = client.put(f'/api/users/{user["id"]}', json={
        "display_name": "Updated Name",
        "bio": "This is my bio"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 200
    data = response.get_json()
    assert data["display_name"] == "Updated Name"
    assert data["bio"] == "This is my bio"


def test_update_user_without_auth(client, db):
    """Test updating user profile without authentication."""
    user = create_test_user(client)

    response = client.put(f'/api/users/{user["id"]}', json={
        "display_name": "Updated Name"
    })

    assert response.status_code == 401
    assert "Authentication token is missing" in response.get_json()["error"]


def test_update_other_user_profile(client, db):
    """Test updating another user's profile (should fail)."""
    # Create two users
    user1 = create_test_user(client, "user1", "user1@example.com")
    user2 = create_test_user(client, "user2", "user2@example.com")

    # Login as user1
    token = login_user(client, "user1")

    # Try to update user2's profile
    response = client.put(f'/api/users/{user2["id"]}', json={
        "display_name": "Hacked Name"
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 403
    assert "You can only update your own profile" in response.get_json()["error"]


def test_update_user_invalid_data(client, db):
    """Test updating user with invalid data."""
    user = create_test_user(client)
    token = login_user(client)

    # Display name too long
    response = client.put(f'/api/users/{user["id"]}', json={
        "display_name": "x" * 101
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 400
    assert "at most 100 characters" in response.get_json()["error"]

    # Bio too long
    response = client.put(f'/api/users/{user["id"]}', json={
        "bio": "x" * 501
    }, headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 400
    assert "at most 500 characters" in response.get_json()["error"]


def test_get_user_tweets(client, db):
    """Test getting all tweets by a user."""
    # Create user
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    # Create some tweets
    for i in range(5):
        tweet = Tweet(content=f"Tweet {i}", user_id=user.id)
        db.session.add(tweet)
    db.session.commit()

    # Get user's tweets
    response = client.get(f'/api/users/{user.id}/tweets')

    assert response.status_code == 200
    data = response.get_json()
    assert "user" in data
    assert "tweets" in data
    assert "pagination" in data
    assert len(data["tweets"]) == 5
    assert data["user"]["username"] == "testuser"


def test_get_user_tweets_pagination(client, db):
    """Test pagination for user tweets."""
    # Create user
    user_data = create_test_user(client)
    user = User.query.get(user_data["id"])

    # Create 25 tweets
    for i in range(25):
        tweet = Tweet(content=f"Tweet {i}", user_id=user.id)
        db.session.add(tweet)
    db.session.commit()

    # Get first page
    response = client.get(f'/api/users/{user.id}/tweets?page=1&per_page=10')

    assert response.status_code == 200
    data = response.get_json()
    assert len(data["tweets"]) == 10
    assert data["pagination"]["total_items"] == 25
    assert data["pagination"]["page"] == 1


def test_get_tweets_for_nonexistent_user(client, db):
    """Test getting tweets for a user that doesn't exist."""
    response = client.get('/api/users/999/tweets')

    assert response.status_code == 404
    assert "User not found" in response.get_json()["error"]


def test_get_user_tweets_empty(client, db):
    """Test getting tweets for a user with no tweets."""
    # Create user without tweets
    user = create_test_user(client)

    response = client.get(f'/api/users/{user["id"]}/tweets')

    assert response.status_code == 200
    data = response.get_json()
    assert len(data["tweets"]) == 0
    assert data["pagination"]["total_items"] == 0
