"""Test application factory and basic endpoints."""


def test_app_creation(app):
    """Test that the app is created successfully."""
    assert app is not None
    assert app.config["TESTING"] is True


def test_health_check(client):
    """Test the health check endpoint."""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.get_json()
    assert data["status"] == "healthy"


def test_index(client):
    """Test the index endpoint."""
    response = client.get("/")
    assert response.status_code == 200
    data = response.get_json()
    assert "message" in data
    assert "version" in data
    assert "endpoints" in data
