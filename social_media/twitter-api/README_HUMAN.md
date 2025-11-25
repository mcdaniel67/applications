# Twitter API - Complete Developer Guide

A comprehensive Twitter clone REST API built with Flask, PostgreSQL, and Docker. This guide is designed to be beginner-friendly while providing advanced information for experienced developers.

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Prerequisites](#prerequisites)
4. [Architecture Overview](#architecture-overview)
5. [Project Structure](#project-structure)
6. [Getting Started](#getting-started)
7. [Development Workflows](#development-workflows)
8. [API Documentation](#api-documentation)
9. [Database Schema](#database-schema)
10. [Testing Guide](#testing-guide)
11. [Code Quality & Standards](#code-quality--standards)
12. [Deployment](#deployment)
13. [Troubleshooting](#troubleshooting)
14. [Contributing](#contributing)
15. [FAQ](#faq)

---

## Introduction

This project is a Twitter clone backend API that demonstrates modern Python web development practices. It's built with Flask (a lightweight Python web framework), uses PostgreSQL for data persistence, and is fully containerized with Docker.

### What is this project?

Think of Twitter's backend - the part that handles user registrations, storing tweets, retrieving timelines, etc. This project recreates that functionality in a simplified way, providing a RESTful API that a mobile app or web frontend could consume.

### Current Implementation Status

**🎉 GOOD NEWS:** The core API is fully functional! You can:
- ✅ Register users and authenticate with JWT tokens
- ✅ Create, read, update, and delete tweets
- ✅ Manage user profiles
- ✅ Everything is tested with 48+ integration tests
- ✅ Production-ready authentication with bcrypt password hashing

**⚠️ What's Missing:**
- Database migrations (manual table creation required for now)
- Social features (likes, follows, retweets)
- Timeline generation
- See the [Features](#features) section for full details

**Ready to try it?** Jump to [Getting Started](#getting-started)!

---

## Features

### ✅ Fully Implemented Features
- **JWT-based authentication and authorization** - Secure token-based auth with 24-hour expiry
- **Password hashing with bcrypt** - Industry-standard password security
- **Tweet creation, editing, and deletion** - Full CRUD operations with ownership verification
- **User registration and login** - With comprehensive validation
- **User profiles** - Display names and bios, update your own profile
- **Pagination** - All list endpoints support pagination (max 100 per page)
- **RESTful API architecture** - Following industry best practices
- **PostgreSQL database** - With SQLAlchemy ORM
- **Service layer architecture** - Clean separation of business logic from routes
- **Docker and Docker Compose** - For development and deployment
- **Comprehensive test suite** - 48+ integration tests with pytest
- **PEP8 compliant code** - Automated linting (flake8) and formatting (Black)
- **CORS support** - For frontend integration
- **Input validation** - With helpful, descriptive error messages
- **Proper HTTP status codes** - 200, 201, 204, 400, 401, 403, 404

### 🚧 Not Yet Implemented (Roadmap)
- Database migrations setup (Flask-Migrate configuration needed)
- Tweet likes and retweets
- User following/followers system
- Timeline/feed generation
- Reply/thread functionality
- Notifications system
- Direct messages
- Hashtags and mentions
- Rate limiting to prevent abuse
- Full-text search for tweets
- Media upload support (images, videos)
- Email verification
- Password reset functionality
- API documentation (Swagger/OpenAPI)

**Current Status:** Core user and tweet functionality is complete and tested. Ready to add social features like likes, follows, and timelines.

See [AGENTS.md](../AGENTS.md) for detailed development progress and roadmap.

---

## Prerequisites

### Required Software

#### Option 1: Docker (Recommended for Beginners)
- **Docker Desktop** (includes Docker and Docker Compose)
  - [Download for Mac](https://docs.docker.com/desktop/mac/install/)
  - [Download for Windows](https://docs.docker.com/desktop/windows/install/)
  - [Download for Linux](https://docs.docker.com/desktop/linux/install/)

**Why Docker?** Docker packages your entire application with all its dependencies into containers. This means you don't need to install Python, PostgreSQL, or any other dependencies locally - Docker handles it all. This eliminates "it works on my machine" problems.

#### Option 2: Local Development
- **Python 3.9 or higher**
  - Check version: `python --version` or `python3 --version`
  - [Download Python](https://www.python.org/downloads/)
- **PostgreSQL 12 or higher**
  - [Download PostgreSQL](https://www.postgresql.org/download/)
  - Or install via package manager:
    - Mac: `brew install postgresql`
    - Ubuntu: `sudo apt-get install postgresql postgresql-contrib`
- **pip** (Python package manager, usually comes with Python)
- **virtualenv** or **venv** for virtual environments

### Optional Tools
- **Postman** or **Insomnia** - For testing API endpoints
- **pgAdmin** or **DBeaver** - For database management and visualization
- **VS Code** or **PyCharm** - Recommended IDEs with Python support

---

## Architecture Overview

### High-Level Architecture

```
┌─────────────────┐
│  Client (Web/   │
│  Mobile App)    │
└────────┬────────┘
         │ HTTP/HTTPS
         │ (JSON)
         ▼
┌─────────────────┐
│   Flask API     │
│   (Python)      │
│                 │
│  ┌───────────┐  │
│  │  Routes   │  │ ◄── API endpoints (URL handlers)
│  └───────────┘  │
│       │         │
│  ┌───────────┐  │
│  │ Services  │  │ ◄── Business logic
│  └───────────┘  │
│       │         │
│  ┌───────────┐  │
│  │  Models   │  │ ◄── Data structures
│  └───────────┘  │
└────────┬────────┘
         │ SQLAlchemy ORM
         ▼
┌─────────────────┐
│   PostgreSQL    │
│   Database      │
└─────────────────┘
```

### Component Breakdown

**1. Routes (API Layer)**
- Handle HTTP requests and responses
- Validate input data
- Call service layer for business logic
- Return JSON responses

**2. Services (Business Logic Layer)**
- Implement core application logic
- Orchestrate operations across multiple models
- Handle complex workflows
- Keep routes thin and focused

**3. Models (Data Layer)**
- Define database schema using SQLAlchemy ORM
- Represent tables as Python classes
- Handle data validation
- Define relationships between entities

**4. Database (PostgreSQL)**
- Persist data permanently
- Handle complex queries efficiently
- Ensure data integrity with constraints

### Request Flow Example

```
User makes POST request to /api/tweets
         │
         ▼
Route receives request (routes/tweets.py)
         │
         ▼
Route validates JSON data
         │
         ▼
Route calls TweetService.create_tweet()
         │
         ▼
Service creates Tweet model instance
         │
         ▼
Service saves to database via SQLAlchemy
         │
         ▼
Database returns saved tweet with ID
         │
         ▼
Service returns tweet data to route
         │
         ▼
Route converts to JSON and returns to user
```

---

## Project Structure

### Detailed Directory Layout

```
twitter-api/
│
├── src/                          # Source code
│   └── twitter_api/              # Main application package
│       ├── __init__.py           # Package initialization
│       ├── app.py                # Application factory (creates Flask app)
│       ├── config.py             # Configuration classes (dev, test, prod)
│       ├── database.py           # Database initialization
│       │
│       ├── models/               # Database models (SQLAlchemy)
│       │   ├── __init__.py
│       │   ├── user.py           # User model (represents users table)
│       │   └── tweet.py          # Tweet model (represents tweets table)
│       │
│       ├── routes/               # API endpoints (Flask blueprints)
│       │   ├── __init__.py
│       │   ├── auth.py           # Authentication routes (/api/auth/*)
│       │   ├── tweets.py         # Tweet routes (/api/tweets/*)
│       │   └── users.py          # User routes (/api/users/*)
│       │
│       ├── services/             # Business logic layer
│       │   ├── __init__.py
│       │   ├── user_service.py   # User-related business logic
│       │   └── tweet_service.py  # Tweet-related business logic
│       │
│       └── utils/                # Helper functions and utilities
│           ├── __init__.py
│           ├── password.py       # Password hashing with bcrypt
│           ├── jwt.py            # JWT token generation/validation
│           └── decorators.py     # Auth decorators (@token_required)
│
├── scripts/                      # Database management scripts
│   ├── __init__.py
│   ├── README.md                 # Scripts documentation
│   ├── init_db.py                # Create database tables
│   ├── seed_data.py              # Populate with test data (350+ lines)
│   └── clear_data.py             # Delete all data
│
├── tests/                        # Test suite
│   ├── __init__.py
│   ├── conftest.py               # Pytest configuration and fixtures
│   ├── unit/                     # Unit tests (test individual functions)
│   │   ├── __init__.py
│   │   └── test_app.py           # Tests for app.py
│   └── integration/              # Integration tests (test API endpoints)
│       └── __init__.py
│
├── Dockerfile                    # Instructions to build Docker image
├── docker-compose.yml            # Multi-container Docker configuration
├── .dockerignore                 # Files to exclude from Docker image
│
├── pyproject.toml                # Modern Python build configuration (PEP 518)
├── setup.py                      # Backwards-compatible setup file
├── requirements.txt              # Production dependencies
├── requirements-dev.txt          # Development dependencies
│
├── pytest.ini                    # Pytest configuration
├── .flake8                       # Flake8 (linter) configuration
├── .env.example                  # Example environment variables
│
└── README.md                     # Quick start guide
```

### Key Files Explained

#### `app.py` - Application Factory
This file contains the `create_app()` function, which creates and configures the Flask application. This pattern is called the "Application Factory Pattern" and is recommended for Flask apps because:
- Makes testing easier (can create multiple app instances with different configs)
- Allows running multiple instances with different settings
- Cleaner code organization

#### `config.py` - Configuration Management
Defines different configuration classes for different environments:
- **DevelopmentConfig**: Debug mode on, verbose logging
- **TestingConfig**: Uses in-memory SQLite database, no side effects
- **ProductionConfig**: Debug off, security hardened

#### `database.py` - Database Setup
Initializes SQLAlchemy (the ORM) and Flask-Migrate (for database migrations).

#### `models/` - Data Models
Each file defines a database table as a Python class. For example:
```python
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), unique=True)
```
This creates a `users` table with `id` and `username` columns.

#### `routes/` - API Endpoints (Blueprints)
Flask blueprints are like mini-applications. They group related routes together. For example, all tweet-related routes (`GET /api/tweets`, `POST /api/tweets`, etc.) are in `routes/tweets.py`.

---

## Getting Started

### Quick Start with Docker (5 minutes)

This is the fastest way to get the application running:

**Step 1: Clone the repository**
```bash
cd ~/Projects/applications/social_media/twitter-api
```

**Step 2: Copy environment file**
```bash
cp .env.example .env
```

**Step 3: Start the application**
```bash
docker-compose up --build
```

This command:
- Builds a Docker image for your Flask app
- Pulls the PostgreSQL image from Docker Hub
- Creates two containers (Flask app + PostgreSQL)
- Links them together in a network
- Starts both services

**Step 4: Verify it's working**

Open your browser and navigate to: `http://localhost:5000`

You should see:
```json
{
  "message": "Welcome to Twitter API",
  "version": "0.1.0",
  "endpoints": {
    "health": "/health",
    "auth": "/api/auth",
    "tweets": "/api/tweets",
    "users": "/api/users"
  }
}
```

**To stop the application:**
Press `Ctrl+C` in the terminal, then run:
```bash
docker-compose down
```

---

### Local Development Setup (Without Docker)

For developers who prefer running services locally or want more control:

#### Step 1: Set up PostgreSQL

**Install PostgreSQL** (if not already installed)
- Mac: `brew install postgresql && brew services start postgresql`
- Ubuntu: `sudo apt-get install postgresql postgresql-contrib`
- Windows: Download installer from postgresql.org

**Create a database and user**
```bash
# Open PostgreSQL shell
psql postgres

# In the PostgreSQL shell:
CREATE USER twitter_user WITH PASSWORD 'twitter_password';
CREATE DATABASE twitter_db OWNER twitter_user;
GRANT ALL PRIVILEGES ON DATABASE twitter_db TO twitter_user;
\q  # Exit
```

#### Step 2: Set up Python environment

**Create a virtual environment**

A virtual environment isolates your project's dependencies from other Python projects.

```bash
# Navigate to project directory
cd /home/kyle/Projects/applications/social_media/twitter-api

# Create virtual environment
python3 -m venv venv

# Activate virtual environment
# On Mac/Linux:
source venv/bin/activate
# On Windows:
venv\Scripts\activate

# Your prompt should now show (venv)
```

**Install dependencies**
```bash
pip install -r requirements-dev.txt
pip install -e .
```

Explanation:
- `-r requirements-dev.txt` installs all development dependencies
- `-e .` installs the current package in "editable" mode (changes reflect immediately)

#### Step 3: Configure environment variables

```bash
cp .env.example .env
```

Edit `.env` and update the database URL if your PostgreSQL settings differ:
```bash
DATABASE_URL=postgresql://twitter_user:twitter_password@localhost:5432/twitter_db
```

#### Step 4: Initialize the database

We've created convenient scripts to set up your database:

```bash
# Create database tables
python scripts/init_db.py

# (Optional but recommended) Populate with test data
# This creates 100 users and ~1,500 realistic tweets
python scripts/seed_data.py
```

The seed script will give you:
- 100 users with realistic names, emails, and bios
- ~1,500 tweets spread over the past 90 days
- All users have password: `password123`
- Varied activity levels (some power users, some casual users)

**Why seed data?** It's much easier to test and develop features when you have realistic data. You can immediately test pagination, user profiles, timelines, etc. without manually creating hundreds of accounts.

**Alternative (manual table creation):**
```bash
export FLASK_APP=twitter_api.app

python3 << 'EOF'
from twitter_api.app import create_app
from twitter_api.database import db

app = create_app()
with app.app_context():
    db.create_all()
    print("Tables created successfully!")
EOF
```

When migrations are set up, you'll use:
```bash
flask db init
flask db migrate -m "Initial migration"
flask db upgrade
```

#### Step 5: Run the application

```bash
flask run
```

The API will be available at `http://localhost:5000`

---

## Database Management & Seeding

We've created powerful database management scripts to make your development life easier. These are located in the `scripts/` directory.

### Available Scripts

#### 1. Initialize Database Tables

Creates all necessary database tables (users, tweets).

```bash
python scripts/init_db.py
```

**When to use:**
- First time setting up the project
- After dropping/recreating your database
- Before seeding data

**What it does:**
- Creates `users` table
- Creates `tweets` table
- Shows you a list of created tables
- Provides helpful next steps

---

#### 2. Seed Test Data

Populates your database with realistic test data - 100 users and approximately 1,500 tweets.

```bash
python scripts/seed_data.py
```

**What you get:**

**100 Users** with:
- Realistic usernames (johndoe, jane_smith, bob123, alice.wonder, etc.)
- Email addresses (generated with Faker library)
- Display names (70% of users have them)
- Bios with job titles and interests (50% of users)
- **All passwords are `password123`** for easy testing

**~1,500 Tweets** with:
- Realistic content from 15+ templates
- Topics: activities, opinions, questions, tips, life updates
- Emojis (😊 🔥 💯 🚀 ❤️ 😂 🎉 ✨ 🤔 👀 💪 🙌)
- All under 280 characters
- Timestamps spread over past 90 days

**Realistic User Distribution:**
- 20% power users: 20-50 tweets each
- 30% active users: 10-19 tweets each
- 30% moderate users: 3-9 tweets each
- 20% casual users: 0-2 tweets each

This mimics real social media where a few people post a lot, and many people post occasionally.

**Sample Output:**
```
==================================================
DATABASE STATISTICS
==================================================
Total Users: 100
Total Tweets: 1543
Average Tweets per User: 15.43

Most Active Users:
  @johndoe: 48 tweets
  @jane_smith: 45 tweets
  @bob123: 42 tweets
  @alice_wonder: 39 tweets
  @charlie_brown: 38 tweets

Sample users (username / password):
  @johndoe / password123
  @jane_smith / password123
  @bob123 / password123
```

**Why use seed data?**
- Test pagination with real data
- See how the UI looks with varied content
- Test user profiles with different activity levels
- Develop timeline features with organic-looking timestamps
- No need to manually create hundreds of test accounts
- Reproducible (same data each time - random seed is 42)

---

#### 3. Clear All Data

Deletes all tweets and users from the database.

```bash
python scripts/clear_data.py
```

**Safety features:**
- Shows current data counts before deletion
- Asks for confirmation (type 'y' to proceed)
- Cannot be undone, so use carefully!

**When to use:**
- Resetting your development environment
- Before re-seeding with fresh data
- Cleaning up after testing

---

### Common Workflows

**First-Time Setup:**
```bash
# 1. Create tables
python scripts/init_db.py

# 2. Add test data
python scripts/seed_data.py

# 3. Start developing!
flask run
```

**Reset Database with Fresh Data:**
```bash
# 1. Clear everything
python scripts/clear_data.py

# 2. Re-seed
python scripts/seed_data.py
```

**Add More Test Data:**
```bash
# Just run seed again (adds 100 more users)
python scripts/seed_data.py
```

---

### Testing with Seeded Data

After seeding, you have 100 ready-to-use test accounts!

**Login as any user:**
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "password": "password123"}'
```

Copy the `access_token` from the response, then:

**Create a tweet:**
```bash
curl -X POST http://localhost:5000/api/tweets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"content": "My test tweet!"}'
```

**Browse all tweets:**
```bash
curl http://localhost:5000/api/tweets?page=1&per_page=20&sort=newest
```

**Get a user's profile:**
```bash
curl http://localhost:5000/api/users/1
```

**Get a user's tweets:**
```bash
curl http://localhost:5000/api/users/1/tweets
```

---

### Script Details

All scripts are well-documented. For more details:
```bash
cat scripts/README.md
```

Or view the scripts directly:
- `scripts/init_db.py` - Simple and clear table creation
- `scripts/seed_data.py` - Sophisticated data generation (350+ lines!)
- `scripts/clear_data.py` - Safe data deletion with confirmation

**Technologies Used:**
- **Faker Library**: Generates realistic names, emails, job titles, companies
- **Random Seed (42)**: Makes data reproducible
- **Template System**: Creates varied, natural-sounding tweet content

---

## Development Workflows

### Starting Your Development Session

**With Docker:**
```bash
docker-compose up
```
Leave this running in one terminal, use another for commands.

**Without Docker:**
```bash
# Activate virtual environment
source venv/bin/activate

# Start the app
flask run
```

### Making Code Changes

The development setup includes hot-reload, meaning:
- Changes to Python files automatically restart the server
- You don't need to manually restart after each change
- Just save your file and refresh your browser/API client

### Adding a New API Endpoint

Let's walk through adding a new endpoint as an example:

**Example: Add a "like tweet" endpoint**

**Step 1: Update the model** (if needed)
```python
# src/twitter_api/models/tweet.py
class Tweet(db.Model):
    # ... existing fields ...
    likes_count = db.Column(db.Integer, default=0)
```

**Step 2: Create a migration**
```bash
flask db migrate -m "Add likes_count to tweets"
flask db upgrade
```

**Step 3: Add the route**
```python
# src/twitter_api/routes/tweets.py
@bp.route("/<int:tweet_id>/like", methods=["POST"])
def like_tweet(tweet_id):
    """Like a tweet."""
    tweet = Tweet.query.get_or_404(tweet_id)
    tweet.likes_count += 1
    db.session.commit()
    return jsonify(tweet.to_dict()), 200
```

**Step 4: Write a test**
```python
# tests/integration/test_tweets.py
def test_like_tweet(client, db):
    """Test liking a tweet."""
    # Setup: create a tweet
    # Test: POST to /api/tweets/1/like
    # Assert: likes_count increased
```

**Step 5: Run tests**
```bash
pytest tests/integration/test_tweets.py::test_like_tweet
```

### Database Migrations Workflow

Database migrations track changes to your database schema over time.

**When do you need a migration?**
- Adding a new table (model)
- Adding/removing columns
- Changing column types
- Adding indexes or constraints

**Workflow:**

1. **Make changes to your models**
   ```python
   # src/twitter_api/models/user.py
   class User(db.Model):
       # Add new field
       avatar_url = db.Column(db.String(255))
   ```

2. **Generate migration**
   ```bash
   flask db migrate -m "Add avatar_url to users"
   ```
   This creates a new migration file in `migrations/versions/`

3. **Review the migration**
   Open the generated file and verify the changes look correct

4. **Apply migration**
   ```bash
   flask db upgrade
   ```

5. **If you make a mistake:**
   ```bash
   flask db downgrade  # Undo last migration
   ```

### Testing Workflow

**Run all tests:**
```bash
pytest
```

**Run specific test file:**
```bash
pytest tests/unit/test_app.py
```

**Run specific test function:**
```bash
pytest tests/unit/test_app.py::test_health_check
```

**Run with coverage report:**
```bash
pytest --cov=twitter_api --cov-report=html
# Open htmlcov/index.html in browser to see visual coverage report
```

**Run tests matching a pattern:**
```bash
pytest -k "test_user"  # Runs all tests with "user" in the name
```

---

## API Documentation

### Base URL
- **Development**: `http://localhost:5000`
- **Production**: `https://your-domain.com`

### Response Format

All responses are JSON. Successful responses have this structure:

```json
{
  "id": 1,
  "username": "johndoe",
  "created_at": "2025-11-19T12:00:00"
}
```

Error responses:
```json
{
  "error": "Resource not found",
  "message": "Tweet with id 999 does not exist"
}
```

### Status Codes

- `200 OK` - Request succeeded
- `201 Created` - Resource created successfully
- `204 No Content` - Request succeeded, no response body (used for DELETE)
- `400 Bad Request` - Invalid input data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Authenticated but not authorized
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server error

**Note:** All core endpoints are now fully implemented. Status code 501 (Not Implemented) is no longer used.

---

### Health Check Endpoint

**Check API Status**

```http
GET /health
```

**Response:**
```json
{
  "status": "healthy",
  "message": "Twitter API is running"
}
```

---

### Authentication Endpoints

#### Register a New User

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "display_name": "John Doe"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "display_name": "John Doe",
  "created_at": "2025-11-19T12:00:00"
}
```

**Validation Rules:**
- Username: 3-50 characters, alphanumeric + underscores
- Email: Valid email format
- Password: Minimum 8 characters
- Display name: Optional, max 100 characters

---

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "user": {
    "id": 1,
    "username": "johndoe"
  }
}
```

**Error (401 Unauthorized):**
```json
{
  "error": "Invalid credentials"
}
```

---

#### Logout

```http
POST /api/auth/logout
Authorization: Bearer <access_token>
```

**Response (200 OK):**
```json
{
  "message": "Successfully logged out"
}
```

---

### Tweet Endpoints

#### Get All Tweets

```http
GET /api/tweets
```

**Query Parameters:**
- `page` (optional): Page number (default: 1)
- `per_page` (optional): Items per page (default: 20, max: 100)
- `sort` (optional): Sort order (`newest`, `oldest`, default: `newest`)

**Example:**
```http
GET /api/tweets?page=2&per_page=10&sort=newest
```

**Response (200 OK):**
```json
{
  "tweets": [
    {
      "id": 1,
      "content": "Hello, Twitter!",
      "user_id": 1,
      "username": "johndoe",
      "created_at": "2025-11-19T12:00:00"
    }
  ],
  "pagination": {
    "page": 2,
    "per_page": 10,
    "total_pages": 5,
    "total_items": 50
  }
}
```

---

#### Get a Specific Tweet

```http
GET /api/tweets/<tweet_id>
```

**Example:**
```http
GET /api/tweets/42
```

**Response (200 OK):**
```json
{
  "id": 42,
  "content": "This is tweet #42",
  "user_id": 1,
  "username": "johndoe",
  "created_at": "2025-11-19T12:00:00",
  "updated_at": "2025-11-19T12:00:00"
}
```

**Error (404 Not Found):**
```json
{
  "error": "Tweet not found",
  "message": "Tweet with id 42 does not exist"
}
```

---

#### Create a New Tweet

```http
POST /api/tweets
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "content": "This is my new tweet!"
}
```

**Validation:**
- Content: Required, 1-280 characters
- Must be authenticated

**Response (201 Created):**
```json
{
  "id": 43,
  "content": "This is my new tweet!",
  "user_id": 1,
  "username": "johndoe",
  "created_at": "2025-11-19T13:00:00"
}
```

---

#### Update a Tweet

```http
PUT /api/tweets/<tweet_id>
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "content": "Updated tweet content"
}
```

**Authorization:**
- Only the tweet owner can update it

**Response (200 OK):**
```json
{
  "id": 43,
  "content": "Updated tweet content",
  "user_id": 1,
  "updated_at": "2025-11-19T14:00:00"
}
```

**Error (403 Forbidden):**
```json
{
  "error": "Forbidden",
  "message": "You can only edit your own tweets"
}
```

---

#### Delete a Tweet

```http
DELETE /api/tweets/<tweet_id>
Authorization: Bearer <access_token>
```

**Authorization:**
- Only the tweet owner can delete it

**Response (204 No Content):**
```
(Empty body)
```

---

### User Endpoints

#### Get All Users

```http
GET /api/users
```

**Query Parameters:**
- `page` (optional): Page number
- `per_page` (optional): Items per page

**Response (200 OK):**
```json
{
  "users": [
    {
      "id": 1,
      "username": "johndoe",
      "display_name": "John Doe",
      "bio": "Software developer",
      "created_at": "2025-11-19T12:00:00"
    }
  ]
}
```

---

#### Get a Specific User

```http
GET /api/users/<user_id>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "display_name": "John Doe",
  "bio": "Software developer",
  "created_at": "2025-11-19T12:00:00",
  "tweet_count": 42,
  "follower_count": 100,
  "following_count": 50
}
```

---

#### Update User Profile

```http
PUT /api/users/<user_id>
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "display_name": "John Doe Jr.",
  "bio": "Senior Software Developer"
}
```

**Authorization:**
- Users can only update their own profile

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "johndoe",
  "display_name": "John Doe Jr.",
  "bio": "Senior Software Developer",
  "updated_at": "2025-11-19T15:00:00"
}
```

---

#### Get User's Tweets

```http
GET /api/users/<user_id>/tweets
```

**Query Parameters:**
- `page`, `per_page` (same as Get All Tweets)

**Response (200 OK):**
```json
{
  "user": {
    "id": 1,
    "username": "johndoe"
  },
  "tweets": [
    {
      "id": 1,
      "content": "Hello, Twitter!",
      "created_at": "2025-11-19T12:00:00"
    }
  ]
}
```

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────────────────────┐
│           USERS                 │
├─────────────────────────────────┤
│ id (PK)          INTEGER        │
│ username         VARCHAR(50)    │◄─────┐
│ email            VARCHAR(120)   │      │
│ password_hash    VARCHAR(255)   │      │
│ display_name     VARCHAR(100)   │      │
│ bio              TEXT            │      │
│ created_at       TIMESTAMP       │      │
│ updated_at       TIMESTAMP       │      │
└─────────────────────────────────┘      │
                                          │
                                          │ 1:N relationship
                                          │ (One user has many tweets)
                                          │
┌─────────────────────────────────┐      │
│          TWEETS                 │      │
├─────────────────────────────────┤      │
│ id (PK)          INTEGER        │      │
│ content          VARCHAR(280)   │      │
│ user_id (FK)     INTEGER        │──────┘
│ created_at       TIMESTAMP       │
│ updated_at       TIMESTAMP       │
└─────────────────────────────────┘
```

### Table Descriptions

#### `users` Table

Stores user account information.

| Column         | Type          | Constraints                    | Description                    |
|----------------|---------------|--------------------------------|--------------------------------|
| id             | INTEGER       | PRIMARY KEY, AUTO_INCREMENT    | Unique user identifier         |
| username       | VARCHAR(50)   | UNIQUE, NOT NULL, INDEX        | User's login name              |
| email          | VARCHAR(120)  | UNIQUE, NOT NULL, INDEX        | User's email address           |
| password_hash  | VARCHAR(255)  | NOT NULL                       | Hashed password (bcrypt)       |
| display_name   | VARCHAR(100)  | NULL                           | User's display name            |
| bio            | TEXT          | NULL                           | User's biography               |
| created_at     | TIMESTAMP     | NOT NULL, DEFAULT NOW()        | Account creation timestamp     |
| updated_at     | TIMESTAMP     | NOT NULL, DEFAULT NOW()        | Last update timestamp          |

**Indexes:**
- `username` - For fast login lookups
- `email` - For email verification and lookups

---

#### `tweets` Table

Stores individual tweets.

| Column         | Type          | Constraints                    | Description                    |
|----------------|---------------|--------------------------------|--------------------------------|
| id             | INTEGER       | PRIMARY KEY, AUTO_INCREMENT    | Unique tweet identifier        |
| content        | VARCHAR(280)  | NOT NULL                       | Tweet text content             |
| user_id        | INTEGER       | FOREIGN KEY(users.id), INDEX   | Author's user ID               |
| created_at     | TIMESTAMP     | NOT NULL, DEFAULT NOW(), INDEX | Tweet creation timestamp       |
| updated_at     | TIMESTAMP     | NOT NULL, DEFAULT NOW()        | Last edit timestamp            |

**Indexes:**
- `user_id` - For quickly finding all tweets by a user
- `created_at` - For sorting by date (timeline generation)

**Foreign Keys:**
- `user_id` → `users.id` (CASCADE on delete - deleting user deletes their tweets)

---

### Future Schema Additions

**Likes Table** (many-to-many relationship)
```sql
CREATE TABLE likes (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    tweet_id INTEGER REFERENCES tweets(id),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, tweet_id)  -- User can only like a tweet once
);
```

**Follows Table** (self-referential many-to-many)
```sql
CREATE TABLE follows (
    id SERIAL PRIMARY KEY,
    follower_id INTEGER REFERENCES users(id),
    following_id INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(follower_id, following_id)  -- Can't follow same user twice
);
```

---

## Testing Guide

### Testing Philosophy

We follow Test-Driven Development (TDD) principles:
1. **Write the test first** (it will fail)
2. **Write minimal code to pass the test**
3. **Refactor** while keeping tests green
4. **Repeat**

### Test Types

#### Unit Tests
Test individual functions in isolation.

**Location:** `tests/unit/`

**Example:**
```python
# tests/unit/test_models.py
def test_user_to_dict():
    """Test User model's to_dict method."""
    user = User(username="johndoe", email="john@example.com")
    user_dict = user.to_dict()

    assert user_dict["username"] == "johndoe"
    assert user_dict["email"] == "john@example.com"
    assert "password_hash" not in user_dict  # Passwords should never be exposed
```

#### Integration Tests
Test multiple components working together, especially API endpoints.

**Location:** `tests/integration/`

**Example:**
```python
# tests/integration/test_auth.py
def test_user_registration(client, db):
    """Test user registration endpoint."""
    response = client.post('/api/auth/register', json={
        "username": "newuser",
        "email": "new@example.com",
        "password": "SecurePass123!"
    })

    assert response.status_code == 201
    data = response.get_json()
    assert data["username"] == "newuser"

    # Verify user was created in database
    user = User.query.filter_by(username="newuser").first()
    assert user is not None
```

### Running Tests

**Run all tests with coverage:**
```bash
pytest --cov=twitter_api --cov-report=term-missing
```

**Run only unit tests:**
```bash
pytest tests/unit/
```

**Run only integration tests:**
```bash
pytest tests/integration/
```

**Run a specific test file:**
```bash
pytest tests/unit/test_models.py
```

**Run tests matching a keyword:**
```bash
pytest -k "authentication"
```

**Run tests and stop at first failure:**
```bash
pytest -x
```

**Run tests with verbose output:**
```bash
pytest -v
```

### Writing Tests

#### Basic Test Structure

```python
def test_something(client, db):
    """Test description - what we're testing and why."""
    # ARRANGE: Set up test data
    user = User(username="testuser", email="test@example.com")
    db.session.add(user)
    db.session.commit()

    # ACT: Perform the action being tested
    response = client.get(f'/api/users/{user.id}')

    # ASSERT: Verify the results
    assert response.status_code == 200
    data = response.get_json()
    assert data["username"] == "testuser"
```

#### Using Fixtures

Fixtures are reusable test data or setup functions.

```python
# tests/conftest.py
@pytest.fixture
def sample_user(db):
    """Create a sample user for testing."""
    user = User(
        username="testuser",
        email="test@example.com",
        password_hash="hashed_password"
    )
    db.session.add(user)
    db.session.commit()
    return user

# tests/integration/test_tweets.py
def test_get_user_tweets(client, db, sample_user):
    """Test getting all tweets by a user."""
    # sample_user is automatically created and passed in
    tweet = Tweet(content="Test tweet", user_id=sample_user.id)
    db.session.add(tweet)
    db.session.commit()

    response = client.get(f'/api/users/{sample_user.id}/tweets')
    assert response.status_code == 200
```

### Test Coverage

Aim for:
- **80%+ overall coverage** - Good starting point
- **100% coverage for critical paths** - Authentication, data validation
- **Focus on behavior, not lines** - Coverage % is a guide, not a goal

**View coverage report:**
```bash
pytest --cov=twitter_api --cov-report=html
open htmlcov/index.html  # Mac
xdg-open htmlcov/index.html  # Linux
start htmlcov/index.html  # Windows
```

---

## Code Quality & Standards

### PEP 8 Style Guide

Python has an official style guide called PEP 8. We enforce it using **flake8**.

**Key rules:**
- **Indentation**: 4 spaces (not tabs)
- **Line length**: Maximum 88 characters (Black's default)
- **Naming conventions**:
  - Variables/functions: `snake_case`
  - Classes: `PascalCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Imports**: At top of file, grouped (standard library, third-party, local)

**Run linter:**
```bash
flake8 src/
```

**Common errors and fixes:**
```
E501: Line too long (92 > 88 characters)
→ Break line into multiple lines

E302: Expected 2 blank lines, found 1
→ Add extra blank line before function/class definition

F401: Module imported but unused
→ Remove the unused import

E231: Missing whitespace after ','
→ Add space: [1,2,3] → [1, 2, 3]
```

### Code Formatting with Black

Black automatically formats your code to be PEP 8 compliant.

**Check formatting:**
```bash
black --check src/
```

**Auto-format code:**
```bash
black src/
```

**Integrate with your editor:**
- **VS Code**: Install "Python" extension, enable "Format on Save"
- **PyCharm**: Settings → Tools → Black, enable on save

### Type Hints (Optional but Recommended)

Type hints make code more readable and catch bugs early.

**Without type hints:**
```python
def create_tweet(user_id, content):
    tweet = Tweet(content=content, user_id=user_id)
    return tweet
```

**With type hints:**
```python
def create_tweet(user_id: int, content: str) -> Tweet:
    tweet = Tweet(content=content, user_id=user_id)
    return tweet
```

**Check types with mypy:**
```bash
mypy src/
```

### Pre-commit Hooks (Recommended)

Automatically run checks before each commit:

**Install pre-commit:**
```bash
pip install pre-commit
```

**Create `.pre-commit-config.yaml`:**
```yaml
repos:
  - repo: https://github.com/psf/black
    rev: 23.0.0
    hooks:
      - id: black

  - repo: https://github.com/pycqa/flake8
    rev: 6.0.0
    hooks:
      - id: flake8

  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.4.0
    hooks:
      - id: trailing-whitespace
      - id: end-of-file-fixer
```

**Install hooks:**
```bash
pre-commit install
```

Now, before each commit, Black and flake8 run automatically!

---

## Deployment

### Deploying with Docker

#### Build Production Image

```bash
docker build -t twitter-api:latest .
```

#### Run in Production Mode

```bash
docker run -d \
  --name twitter-api \
  -p 5000:5000 \
  -e FLASK_ENV=production \
  -e DATABASE_URL=postgresql://user:pass@db:5432/twitter \
  -e SECRET_KEY=your-secret-key \
  twitter-api:latest
```

### Environment Variables for Production

```bash
FLASK_ENV=production
SECRET_KEY=<generate-random-secret-key>
DATABASE_URL=postgresql://user:password@host:5432/dbname
```

**Generate a secure secret key:**
```python
import secrets
print(secrets.token_hex(32))
```

### Deployment Platforms

#### Heroku

```bash
# Install Heroku CLI
# Login
heroku login

# Create app
heroku create your-app-name

# Add PostgreSQL
heroku addons:create heroku-postgresql:hobby-dev

# Deploy
git push heroku main

# Run migrations
heroku run flask db upgrade
```

#### AWS (EC2 + RDS)

1. **Launch EC2 instance** (Ubuntu recommended)
2. **Create RDS PostgreSQL database**
3. **SSH into EC2 and clone your repo**
4. **Install dependencies**
5. **Set up systemd service** for Flask app
6. **Configure Nginx** as reverse proxy
7. **Set up SSL** with Let's Encrypt

#### DigitalOcean App Platform

1. Connect GitHub repository
2. Select "Python" as app type
3. Add PostgreSQL database
4. Set environment variables
5. Deploy

---

## Troubleshooting

### Common Issues and Solutions

#### Issue: "Connection refused" when running with Docker

**Symptom:**
```
psycopg2.OperationalError: could not connect to server: Connection refused
```

**Solution:**
- Make sure PostgreSQL container is running: `docker-compose ps`
- Check if `DATABASE_URL` uses `db` as hostname (not `localhost`)
- Verify `depends_on` in docker-compose.yml

#### Issue: "Module not found" errors

**Symptom:**
```
ModuleNotFoundError: No module named 'flask'
```

**Solution (with Docker):**
```bash
docker-compose down
docker-compose build --no-cache
docker-compose up
```

**Solution (local):**
```bash
# Activate virtual environment
source venv/bin/activate

# Reinstall dependencies
pip install -r requirements-dev.txt
pip install -e .
```

#### Issue: Database migration errors

**Symptom:**
```
alembic.util.exc.CommandError: Can't locate revision identified by 'abc123'
```

**Solution:**
```bash
# Reset migrations (WARNING: Deletes all data)
rm -rf migrations/
flask db init
flask db migrate -m "Initial migration"
flask db upgrade
```

#### Issue: "Address already in use"

**Symptom:**
```
OSError: [Errno 98] Address already in use
```

**Solution:**
```bash
# Find process using port 5000
lsof -i :5000

# Kill the process
kill -9 <PID>

# Or use a different port
flask run --port 5001
```

#### Issue: Tests fail with database errors

**Symptom:**
```
sqlalchemy.exc.OperationalError: no such table: users
```

**Solution:**
- Make sure tests use `TestingConfig` with SQLite in-memory database
- Check that `db` fixture creates tables: `db.create_all()`
- Verify imports in test files

### Debugging Tips

**Enable debug mode:**
```python
# config.py
class DevelopmentConfig(Config):
    DEBUG = True
```

**Use Python debugger:**
```python
# Add breakpoint in code
import pdb; pdb.set_trace()

# When code reaches this line, interactive debugger starts
# Commands: n (next), c (continue), p variable (print), q (quit)
```

**View SQL queries:**
```python
# Enable SQLAlchemy logging
import logging
logging.basicConfig()
logging.getLogger('sqlalchemy.engine').setLevel(logging.INFO)
```

**Check Docker logs:**
```bash
docker-compose logs web      # Flask app logs
docker-compose logs db       # PostgreSQL logs
docker-compose logs -f web   # Follow logs in real-time
```

---

## Contributing

### Getting Started

1. **Fork the repository**
2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/twitter-api.git
   ```
3. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

### Development Workflow

1. **Make your changes**
2. **Write tests** for new functionality
3. **Run tests** to ensure nothing broke
   ```bash
   pytest
   ```
4. **Check code quality**
   ```bash
   flake8 src/
   black --check src/
   ```
5. **Commit with clear message**
   ```bash
   git commit -m "Add user profile picture upload feature"
   ```
6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```
7. **Create Pull Request** on GitHub

### Code Review Process

- All PRs require at least one review
- Automated tests must pass
- Code coverage should not decrease
- Follow the existing code style

---

## FAQ

### General Questions

**Q: Is this production-ready?**
A: Not yet. This is a foundation. You'd need to add:
- Authentication (JWT tokens)
- Password hashing
- Rate limiting
- Input validation
- Security headers
- Monitoring/logging
- Performance optimization

**Q: Can I use this for my startup?**
A: Yes! It's open-source (check the license). But please add proper security before going live.

**Q: Why Flask instead of Django?**
A: Flask is lightweight and flexible - great for learning and APIs. Django includes more features out-of-the-box, which can be helpful for larger applications but adds complexity.

### Technical Questions

**Q: Why PostgreSQL and not MongoDB?**
A: PostgreSQL is a relational database, great for structured data with clear relationships (users have tweets). MongoDB is NoSQL, better for unstructured/flexible data. For Twitter's data model, relational makes sense.

**Q: What's the difference between `requirements.txt` and `pyproject.toml`?**
A:
- `requirements.txt` - Lists dependencies for pip
- `pyproject.toml` - Modern standard (PEP 518) that includes build config, dependencies, and tool settings in one file

**Q: Why use Docker?**
A: Docker ensures everyone runs the exact same environment, eliminating "works on my machine" issues. It also makes deployment easier.

**Q: How do I add authentication?**
A: We'll add JWT (JSON Web Token) authentication in a future update. You can also check out Flask-JWT-Extended library.

**Q: Can I use SQLite instead of PostgreSQL?**
A: For development/testing, yes! Change `DATABASE_URL` to `sqlite:///twitter.db`. For production, use PostgreSQL (better performance, features, and reliability).

---

## Next Steps

### For Beginners
1. Get the app running locally
2. Make a simple change (e.g., add a field to User model)
3. Write a test for your change
4. Successfully run all tests

### For Intermediate Developers
1. Implement user authentication (JWT tokens)
2. Add password hashing (bcrypt)
3. Implement tweet creation and retrieval
4. Write comprehensive integration tests

### For Advanced Developers
1. Add WebSocket support for real-time updates
2. Implement full-text search with PostgreSQL
3. Add Redis caching layer
4. Implement follower/following relationships
5. Create timeline generation algorithm
6. Add rate limiting and API throttling
7. Set up CI/CD pipeline

---

## Resources

### Official Documentation
- [Flask Documentation](https://flask.palletsprojects.com/)
- [SQLAlchemy Documentation](https://docs.sqlalchemy.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [pytest Documentation](https://docs.pytest.org/)
- [Docker Documentation](https://docs.docker.com/)

### Tutorials
- [Flask Mega-Tutorial](https://blog.miguelgrinberg.com/post/the-flask-mega-tutorial-part-i-hello-world)
- [Real Python Flask Tutorials](https://realpython.com/tutorials/flask/)
- [SQLAlchemy Tutorial](https://docs.sqlalchemy.org/en/14/tutorial/)

### Books
- "Flask Web Development" by Miguel Grinberg
- "RESTful Web API Design with Python Flask" by Kunal Relan
- "Test-Driven Development with Python" by Harry Percival

### Community
- [r/flask on Reddit](https://www.reddit.com/r/flask/)
- [Flask Discord](https://discord.gg/pallets)
- [Stack Overflow - Flask Tag](https://stackoverflow.com/questions/tagged/flask)

---

## License

MIT License - See LICENSE file for details

---

## Contact

For questions, issues, or contributions, please open an issue on GitHub or contact the maintainers.

**Happy coding!**
