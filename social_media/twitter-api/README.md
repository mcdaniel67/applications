# Twitter API

A Twitter clone REST API built with Flask and PostgreSQL.

## Current Status

✅ **Fully Implemented:**
- User authentication (JWT tokens)
- User registration and login
- Password hashing (bcrypt)
- Tweet CRUD operations
- User profile management
- Follow/unfollow system
- Followers and following lists
- Personalized feed (timeline)
- Global feed (discovery)
- Swagger/OpenAPI documentation
- Comprehensive test suite (48+ tests)

🚧 **Not Yet Implemented:**
- Database migrations (setup needed)
- Social features (likes, retweets, replies)
- See [AGENTS.md](../AGENTS.md) for full roadmap

## Features

- **RESTful API design** with proper HTTP status codes
- **JWT Authentication** with bcrypt password hashing
- **PostgreSQL database** with SQLAlchemy ORM
- **Service layer architecture** separating business logic from routes
- **Docker and Docker Compose** support for easy deployment
- **Swagger/OpenAPI documentation** at `/apidocs/`
- **Comprehensive test suite** with pytest (48+ integration tests)
- **PEP8 compliant code** enforced with flake8 and black
- **Input validation** with helpful error messages
- **Pagination support** for all list endpoints

## Project Structure

```
twitter-api/
├── src/twitter_api/        # Application source code
│   ├── models/             # SQLAlchemy models
│   ├── routes/             # API endpoints
│   ├── services/           # Business logic
│   └── utils/              # Helper functions
├── scripts/                # Database management scripts
│   ├── init_db.py         # Create tables
│   ├── seed_data.py       # Populate test data
│   └── clear_data.py      # Delete all data
├── tests/                  # Test suite
│   ├── unit/              # Unit tests
│   └── integration/       # Integration tests
├── docker-compose.yml      # Docker services configuration
├── Dockerfile             # Application container
└── pyproject.toml         # Build configuration
```

## Prerequisites

- Python 3.9+
- Docker and Docker Compose (for containerized setup)
- PostgreSQL (for local development without Docker)

## Getting Started

### Using Docker (Recommended)

1. Copy the environment template:
   ```bash
   cp .env.example .env
   ```

2. Start the services:
   ```bash
   # On Fedora/RHEL (if not in docker group, use sudo)
   sudo docker compose up --build -d
   
   # Or without sudo (if your user is in the docker group)
   docker compose up --build -d
   ```

3. Initialize the database:
   ```bash
   sudo docker compose exec web python scripts/init_db.py
   ```

4. (Optional) Seed with test data:
   ```bash
   sudo docker compose exec web python scripts/seed_data.py
   ```

5. The API will be available at `http://localhost:5000`

6. Access the Swagger documentation at `http://localhost:5000/apidocs/`

7. View logs:
   ```bash
   sudo docker compose logs -f web
   ```

8. Stop the services:
   ```bash
   sudo docker compose down
   ```

**Note:** On Fedora/RHEL systems, you may need to use `sudo` with docker commands unless your user is added to the docker group. To add your user to the docker group:
```bash
sudo usermod -aG docker $USER
# Log out and back in for changes to take effect
```

### Local Development

1. Create a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

2. Install dependencies:
   ```bash
   pip install -r requirements-dev.txt
   pip install -e .
   ```

3. Set up environment variables:
   ```bash
   cp .env.example .env
   # Edit .env with your local database credentials
   ```

4. Initialize and seed the database:
   ```bash
   # Create database tables
   python scripts/init_db.py

   # (Optional) Populate with 100 users and ~1,500 tweets
   python scripts/seed_data.py
   ```

5. Run the application:
   ```bash
   flask run
   ```

## Database Scripts

The `scripts/` directory contains utilities for database management:

- **`init_db.py`** - Create all database tables
- **`seed_data.py`** - Populate database with 100 users and ~1,500 realistic tweets
- **`clear_data.py`** - Delete all data (with confirmation)

**Quick Setup:**
```bash
python scripts/init_db.py      # Create tables
python scripts/seed_data.py    # Add test data
```

All seeded users have password: `password123`

See [scripts/README.md](scripts/README.md) for detailed documentation.

## API Endpoints

**💡 Tip:** For interactive API documentation and testing, visit the Swagger UI at `http://localhost:5000/apidocs/`

### Health Check
- `GET /health` - Health check endpoint

### Authentication (🔒 = requires authentication)
- `POST /api/auth/register` - Register a new user
  - Body: `username`, `email`, `password`, `display_name` (optional)
  - Returns: User object
- `POST /api/auth/login` - Login and receive JWT token
  - Body: `username`, `password`
  - Returns: `access_token`, `token_type`, `user`
- `POST /api/auth/logout` 🔒 - Logout (invalidate token client-side)

### Tweets
- `GET /api/tweets` - Get all tweets with pagination
  - Query params: `page` (default: 1), `per_page` (default: 20, max: 100), `sort` (newest/oldest)
  - Returns: `tweets[]`, `pagination`
- `GET /api/tweets/<id>` - Get a specific tweet
- `POST /api/tweets` 🔒 - Create a new tweet
  - Body: `content` (1-280 chars)
  - Returns: Tweet object
- `PUT /api/tweets/<id>` 🔒 - Update your own tweet
  - Body: `content`
  - Returns: Updated tweet
- `DELETE /api/tweets/<id>` 🔒 - Delete your own tweet
  - Returns: 204 No Content

### Users
- `GET /api/users` - Get all users with pagination
  - Query params: `page`, `per_page`
  - Returns: `users[]`, `pagination`
- `GET /api/users/<id>` - Get a specific user with stats
  - Returns: User object with `tweet_count`, `followers_count`, `following_count`
- `PUT /api/users/<id>` 🔒 - Update your own profile
  - Body: `display_name`, `bio`
  - Returns: Updated user
- `GET /api/users/<id>/tweets` - Get user's tweets with pagination
  - Query params: `page`, `per_page`
  - Returns: `user`, `tweets[]`, `pagination`

### Follows
- `POST /api/users/<id>/follow` 🔒 - Follow a user
  - Returns: 201 Created with success message
  - Errors: 400 (self-follow/duplicate), 404 (user not found)
- `DELETE /api/users/<id>/follow` 🔒 - Unfollow a user
  - Returns: 204 No Content
  - Errors: 404 (not following)
- `GET /api/users/<id>/followers` - Get user's followers
  - Query params: `page`, `per_page`
  - Returns: `users[]`, `pagination`
- `GET /api/users/<id>/following` - Get who user is following
  - Query params: `page`, `per_page`
  - Returns: `users[]`, `pagination`

### Feed
- `GET /api/feed` 🔒 - Get personalized feed
  - Shows tweets from users you follow
  - Query params: `page`, `per_page`
  - Returns: `tweets[]`, `pagination`
- `GET /api/feed/global` - Get global feed
  - Shows all tweets (public endpoint)
  - Query params: `page`, `per_page`
  - Returns: `tweets[]`, `pagination`

## Quick Examples

**Health check:**
```bash
curl http://localhost:5000/health
```

**Register a user:**
```bash
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "email": "john@example.com", "password": "SecurePass123!"}'
```

**Login:**
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "password": "SecurePass123!"}'
```

**Create a tweet (save the token from login response):**
```bash
curl -X POST http://localhost:5000/api/tweets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"content": "Hello, Twitter!"}'
```

**Get all tweets:**
```bash
curl http://localhost:5000/api/tweets?page=1&per_page=20&sort=newest
```

**Get a specific user:**
```bash
curl http://localhost:5000/api/users/1
```

**Follow a user:**
```bash
curl -X POST http://localhost:5000/api/users/2/follow \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Get user's followers:**
```bash
curl http://localhost:5000/api/users/1/followers
```

**Unfollow a user:**
```bash
curl -X DELETE http://localhost:5000/api/users/2/follow \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Get personalized feed:**
```bash
curl http://localhost:5000/api/feed \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Get global feed:**
```bash
curl "http://localhost:5000/api/feed/global?per_page=10"
```

## Running Tests

```bash
# Run all tests
pytest

# Run with coverage
pytest --cov=twitter_api --cov-report=html

# Run specific test file
pytest tests/unit/test_app.py
```

## Code Quality

### Linting (PEP8)
```bash
flake8 src/
```

### Code Formatting
```bash
# Check formatting
black --check src/

# Apply formatting
black src/
```

### Type Checking
```bash
mypy src/
```

## Database Migrations

```bash
# Initialize migrations (first time only)
flask db init

# Create a new migration
flask db migrate -m "Description of changes"

# Apply migrations
flask db upgrade

# Rollback migration
flask db downgrade
```

## Development Workflow

1. Create a new branch for your feature
2. Write tests for your changes
3. Implement your feature
4. Run tests: `pytest`
5. Check code quality: `flake8 src/ && black --check src/`
6. Commit your changes

## Troubleshooting

### Docker Permission Denied (Fedora/RHEL)
If you get "permission denied" errors when running docker commands:
```bash
# Add your user to the docker group
sudo usermod -aG docker $USER

# Log out and back in, or run:
newgrp docker

# Verify docker works without sudo
docker ps
```

### Database Connection Issues
If the web container can't connect to the database:
```bash
# Check if both containers are running
sudo docker compose ps

# Check database logs
sudo docker compose logs db

# Restart the services
sudo docker compose restart
```

### Port Already in Use
If port 5000 or 5432 is already in use:
```bash
# Find what's using the port
sudo lsof -i :5000
sudo lsof -i :5432

# Either stop that service or change the port in docker-compose.yml
```

### Reset Everything
To completely reset the database and containers:
```bash
sudo docker compose down -v  # -v removes volumes (deletes data)
sudo docker compose up --build -d
sudo docker compose exec web python scripts/init_db.py
```

## License

MIT License
