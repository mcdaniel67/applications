# Database Scripts

Utility scripts for managing the Twitter API database.

## Prerequisites

Make sure you have installed the dev dependencies:
```bash
pip install -r requirements-dev.txt
```

## Available Scripts

### 1. Initialize Database (`init_db.py`)

Creates all database tables based on the SQLAlchemy models.

**Usage:**
```bash
python scripts/init_db.py
```

**When to use:**
- First time setting up the database
- After dropping/recreating the database
- Before running seed_data.py

**Output:**
- Creates `users` and `tweets` tables
- Shows list of created tables

---

### 2. Seed Data (`seed_data.py`)

Populates the database with realistic test data.

**Usage:**
```bash
python scripts/seed_data.py
```

**What it creates:**
- **100 users** with:
  - Realistic usernames (e.g., `johndoe`, `jane_smith`, `bob123`)
  - Email addresses
  - Display names (70% have them)
  - Bios (50% have them)
  - All passwords are `password123` for easy testing

- **~1,500 tweets** with:
  - Realistic content using templates
  - Varied distribution:
    - 20% power users (20-50 tweets each)
    - 30% active users (10-19 tweets each)
    - 30% moderate users (3-9 tweets each)
    - 20% casual users (0-2 tweets each)
  - Timestamps spread over past 90 days
  - Emojis and varied content

- **~1,500 follow relationships** with:
  - Realistic social media distribution:
    - 10% influencers (followed by many, follow few): 0-5 following
    - 20% popular users: 10-30 following
    - 40% average users: 15-40 following
    - 30% new/casual users: 5-20 following
  - Weighted selection (more active users are more likely to be followed)
  - No duplicate follows or self-follows

**Output:**
- Shows progress during creation
- Displays statistics at the end
- Lists top 5 most active users
- Lists top 5 most followed users
- Shows sample usernames/passwords

**Example output:**
```
==================================================
DATABASE STATISTICS
==================================================
Total Users: 100
Total Tweets: 1046
Total Follows: 1510
Average Tweets per User: 10.46
Average Follows per User: 15.10

Most Active Users:
  @johndoe: 48 tweets
  @jane_smith: 45 tweets
  @bob123: 42 tweets
  @alice_wonder: 39 tweets

Most Followed Users:
  @johndoe: 60 followers
  @jane_smith: 52 followers
  @bob123: 48 followers
  @charlie_brown: 38 tweets

==================================================
Sample users (username / password):
==================================================
  @johndoe / password123
  @jane_smith / password123
  @bob123 / password123
  @alice_wonder / password123
  @charlie_brown / password123
```

---

### 3. Clear Data (`clear_data.py`)

Deletes all data from the database (tweets and users).

**Usage:**
```bash
python scripts/clear_data.py
```

**Warning:** This operation is irreversible! You'll be prompted to confirm.

**What it does:**
- Deletes all tweets
- Deletes all users
- Shows counts before deletion
- Asks for confirmation before proceeding

---

## Common Workflows

### First-Time Setup
```bash
# 1. Create database tables
python scripts/init_db.py

# 2. Populate with test data
python scripts/seed_data.py
```

### Reset Database with Fresh Data
```bash
# 1. Clear existing data
python scripts/clear_data.py

# 2. Add fresh seed data
python scripts/seed_data.py
```

### Add More Test Data
```bash
# Just run seed again (it will add to existing data)
python scripts/seed_data.py
```

---

## Testing with Seed Data

After seeding, you can test the API with these sample users:

**Login Example:**
```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "johndoe", "password": "password123"}'
```

**Get User's Tweets:**
```bash
curl http://localhost:5000/api/users/1/tweets
```

**Get All Tweets:**
```bash
curl http://localhost:5000/api/tweets?page=1&per_page=20
```

All seeded users have the password: `password123`

---

## Notes

- The seed script uses [Faker](https://faker.readthedocs.io/) to generate realistic data
- Random seed is set to `42` for reproducible results
- Tweet content is generated from templates for variety
- User distribution mimics real social media patterns (few power users, many casual users)
- Timestamps are randomized over the past 90 days to simulate organic growth

---

## Troubleshooting

**Error: "No module named 'faker'"**
```bash
pip install faker
# or
pip install -r requirements-dev.txt
```

**Error: "Table already exists"**
- This is normal if you've already run `init_db.py`
- You can safely ignore this error

**Error: "Database connection failed"**
- Make sure PostgreSQL is running
- Check your `.env` file for correct database credentials
- If using Docker: run `docker-compose up -d db` first
