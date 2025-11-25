# Development Guide

## Local Development with mise

### Prerequisites
- [mise](https://mise.jdx.dev/) installed

### Setup

1. **Install dependencies** (mise will automatically install Python 3.11 and PostgreSQL 17):
   ```bash
   cd twitter-api
   mise install
   ```

2. **Install Python packages**:
   ```bash
   mise exec -- pip install -e .
   mise exec -- pip install faker flasgger
   ```

3. **Initialize PostgreSQL**:
   ```bash
   # Initialize database cluster
   mise exec -- initdb -D .postgres
   
   # Start PostgreSQL
   mise exec -- pg_ctl -D .postgres -l .postgres/logfile start
   
   # Create database and user
   mise exec -- createdb twitter_db
   mise exec -- psql -d twitter_db -c "CREATE USER twitter_user WITH PASSWORD 'twitter_password';"
   mise exec -- psql -d twitter_db -c "GRANT ALL PRIVILEGES ON DATABASE twitter_db TO twitter_user;"
   mise exec -- psql -d twitter_db -c "GRANT ALL ON SCHEMA public TO twitter_user;"
   ```

4. **Initialize database tables**:
   ```bash
   mise exec -- python scripts/init_db.py
   ```

5. **Seed with test data** (optional):
   ```bash
   mise exec -- python scripts/seed_data.py
   ```

6. **Run the application**:
   ```bash
   ./run.sh
   # Or manually:
   mise exec -- flask run
   ```

7. **Access the API**:
   - API: http://localhost:5000
   - Health check: http://localhost:5000/health
   - Swagger docs: http://localhost:5000/apidocs/

### Useful Commands

**Stop PostgreSQL**:
```bash
mise exec -- pg_ctl -D .postgres stop
```

**Restart PostgreSQL**:
```bash
mise exec -- pg_ctl -D .postgres restart
```

**Connect to database**:
```bash
mise exec -- psql -d twitter_db
```

**Clear all data**:
```bash
mise exec -- python scripts/clear_data.py
```

**Run tests**:
```bash
mise exec -- pytest
```

## Docker Development

### Prerequisites
- Docker and Docker Compose installed

### Setup

1. **Start services**:
   ```bash
   docker compose up --build
   ```

2. **Initialize database** (in another terminal):
   ```bash
   docker compose exec web python scripts/init_db.py
   docker compose exec web python scripts/seed_data.py
   ```

3. **Access the API**:
   - API: http://localhost:5000
   - Swagger docs: http://localhost:5000/apidocs/

### Useful Commands

**View logs**:
```bash
docker compose logs -f web
```

**Stop services**:
```bash
docker compose down
```

**Reset everything** (including database):
```bash
docker compose down -v
docker compose up --build
```

**Run commands in container**:
```bash
docker compose exec web python scripts/init_db.py
docker compose exec web flask shell
```

## Environment Variables

Create a `.env` file (already exists) with:

```env
# Flask Configuration
FLASK_APP=twitter_api.app
FLASK_ENV=development
SECRET_KEY=dev-secret-key-change-in-production

# Database Configuration (local)
DATABASE_URL=postgresql://twitter_user:twitter_password@localhost:5432/twitter_db

# PostgreSQL Configuration
POSTGRES_USER=twitter_user
POSTGRES_PASSWORD=twitter_password
POSTGRES_DB=twitter_db
```

## Testing

Run all tests:
```bash
mise exec -- pytest
```

Run with coverage:
```bash
mise exec -- pytest --cov=twitter_api --cov-report=html
```

## Code Quality

**Linting**:
```bash
mise exec -- flake8 src/
```

**Formatting**:
```bash
mise exec -- black src/
```

## API Testing

**Register a user**:
```bash
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com", "password": "password123"}'
```

**Login**:
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

**Create a tweet** (use token from login):
```bash
curl -X POST http://localhost:5000/api/tweets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"content": "Hello from the API!"}'
```

**Get tweets**:
```bash
curl http://localhost:5000/api/tweets
```

## Troubleshooting

### PostgreSQL won't start
```bash
# Check if already running
mise exec -- pg_ctl -D .postgres status

# If stuck, force stop and restart
mise exec -- pg_ctl -D .postgres -m immediate stop
mise exec -- pg_ctl -D .postgres start
```

### Port 5000 already in use
```bash
# Find process using port 5000
lsof -i :5000

# Kill it
kill -9 <PID>
```

### Database connection errors
Make sure PostgreSQL is running and the database exists:
```bash
mise exec -- pg_ctl -D .postgres status
mise exec -- psql -l
```
