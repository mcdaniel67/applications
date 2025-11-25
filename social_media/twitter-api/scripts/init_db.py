"""
Initialize database tables.

This script creates all database tables based on the SQLAlchemy models.
Run from the project root: python scripts/init_db.py
"""

import sys
import os

# Add parent directory to path to import app modules
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from twitter_api.app import create_app  # noqa: E402
from twitter_api.database import db  # noqa: E402


def init_database():
    """Initialize the database by creating all tables."""
    print("=" * 50)
    print("TWITTER API - DATABASE INITIALIZER")
    print("=" * 50)
    print()

    app = create_app()

    with app.app_context():
        print("Creating database tables...")
        print()

        try:
            # Create all tables
            db.create_all()

            # Get table names
            inspector = db.inspect(db.engine)
            tables = inspector.get_table_names()

            print("✓ Successfully created the following tables:")
            for table in tables:
                print(f"  - {table}")

            print()
            print("✓ Database initialization complete!")
            print()
            print("Next steps:")
            print("  1. Run 'python scripts/seed_data.py' to populate with test data")
            print("  2. Start the app with 'flask run' or 'docker-compose up'")
            print()

        except Exception as e:
            print(f"✗ Error creating tables: {str(e)}")
            print()
            print("The tables might already exist.")
            print("To recreate them, you can:")
            print("  1. Drop the database")
            print("  2. Run this script again")
            print()


if __name__ == "__main__":
    init_database()
