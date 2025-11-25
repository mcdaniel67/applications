"""Integration tests for authentication endpoints."""
from twitter_api.models.user import User


def test_user_registration(client, db):
    """Test user registration endpoint."""
    response = client.post('/api/auth/register', json={
        "username": "newuser",
        "email": "new@example.com",
        "password": "SecurePass123!",
        "display_name": "New User"
    })

    assert response.status_code == 201
    data = response.get_json()
    assert data["username"] == "newuser"
    assert data["email"] == "new@example.com"
    assert data["display_name"] == "New User"
    assert "password_hash" not in data
    assert "password" not in data

    # Verify user was created in database
    user = User.query.filter_by(username="newuser").first()
    assert user is not None
    assert user.email == "new@example.com"


def test_user_registration_missing_fields(client, db):
    """Test registration with missing required fields."""
    # Missing username
    response = client.post('/api/auth/register', json={
        "email": "test@example.com",
        "password": "password123"
    })
    assert response.status_code == 400
    assert "Username is required" in response.get_json()["error"]

    # Missing email
    response = client.post('/api/auth/register', json={
        "username": "testuser",
        "password": "password123"
    })
    assert response.status_code == 400
    assert "Email is required" in response.get_json()["error"]

    # Missing password
    response = client.post('/api/auth/register', json={
        "username": "testuser",
        "email": "test@example.com"
    })
    assert response.status_code == 400
    assert "Password is required" in response.get_json()["error"]


def test_user_registration_duplicate_username(client, db):
    """Test registration with duplicate username."""
    # Create first user
    client.post('/api/auth/register', json={
        "username": "testuser",
        "email": "test1@example.com",
        "password": "password123"
    })

    # Try to create second user with same username
    response = client.post('/api/auth/register', json={
        "username": "testuser",
        "email": "test2@example.com",
        "password": "password123"
    })

    assert response.status_code == 400
    assert "Username already exists" in response.get_json()["error"]


def test_user_registration_duplicate_email(client, db):
    """Test registration with duplicate email."""
    # Create first user
    client.post('/api/auth/register', json={
        "username": "user1",
        "email": "test@example.com",
        "password": "password123"
    })

    # Try to create second user with same email
    response = client.post('/api/auth/register', json={
        "username": "user2",
        "email": "test@example.com",
        "password": "password123"
    })

    assert response.status_code == 400
    assert "Email already exists" in response.get_json()["error"]


def test_user_registration_invalid_email(client, db):
    """Test registration with invalid email format."""
    response = client.post('/api/auth/register', json={
        "username": "testuser",
        "email": "invalid-email",
        "password": "password123"
    })

    assert response.status_code == 400
    assert "Invalid email format" in response.get_json()["error"]


def test_user_registration_short_password(client, db):
    """Test registration with password that's too short."""
    response = client.post('/api/auth/register', json={
        "username": "testuser",
        "email": "test@example.com",
        "password": "short"
    })

    assert response.status_code == 400
    assert "at least 8 characters" in response.get_json()["error"]


def test_user_login(client, db):
    """Test user login endpoint."""
    # First register a user
    client.post('/api/auth/register', json={
        "username": "testuser",
        "email": "test@example.com",
        "password": "password123"
    })

    # Now login
    response = client.post('/api/auth/login', json={
        "username": "testuser",
        "password": "password123"
    })

    assert response.status_code == 200
    data = response.get_json()
    assert "access_token" in data
    assert data["token_type"] == "Bearer"
    assert "user" in data
    assert data["user"]["username"] == "testuser"


def test_user_login_invalid_credentials(client, db):
    """Test login with invalid credentials."""
    # Register a user
    client.post('/api/auth/register', json={
        "username": "testuser",
        "email": "test@example.com",
        "password": "password123"
    })

    # Try to login with wrong password
    response = client.post('/api/auth/login', json={
        "username": "testuser",
        "password": "wrongpassword"
    })

    assert response.status_code == 401
    assert "Invalid username or password" in response.get_json()["error"]


def test_user_login_nonexistent_user(client, db):
    """Test login with non-existent username."""
    response = client.post('/api/auth/login', json={
        "username": "nonexistent",
        "password": "password123"
    })

    assert response.status_code == 401
    assert "Invalid username or password" in response.get_json()["error"]


def test_user_logout(client, db):
    """Test user logout endpoint."""
    # Register and login
    client.post('/api/auth/register', json={
        "username": "testuser",
        "email": "test@example.com",
        "password": "password123"
    })

    login_response = client.post('/api/auth/login', json={
        "username": "testuser",
        "password": "password123"
    })

    token = login_response.get_json()["access_token"]

    # Logout
    response = client.post('/api/auth/logout', headers={
        "Authorization": f"Bearer {token}"
    })

    assert response.status_code == 200
    assert "Successfully logged out" in response.get_json()["message"]


def test_logout_without_token(client, db):
    """Test logout without authentication token."""
    response = client.post('/api/auth/logout')

    assert response.status_code == 401
    assert "Authentication token is missing" in response.get_json()["error"]


def test_logout_with_invalid_token(client, db):
    """Test logout with invalid token."""
    response = client.post('/api/auth/logout', headers={
        "Authorization": "Bearer invalid_token_here"
    })

    assert response.status_code == 401
    assert "Invalid or expired token" in response.get_json()["error"]
