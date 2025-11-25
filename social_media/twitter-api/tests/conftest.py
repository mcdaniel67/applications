"""Pytest configuration and fixtures."""
import pytest
from twitter_api.app import create_app
from twitter_api.database import db as _db


@pytest.fixture(scope="session")
def app():
    """Create application for testing."""
    app = create_app("testing")
    return app


@pytest.fixture(scope="session")
def client(app):
    """Create test client."""
    return app.test_client()


@pytest.fixture(scope="function")
def db(app):
    """Create database for testing."""
    with app.app_context():
        _db.create_all()
        yield _db
        _db.session.remove()
        _db.drop_all()
