"""
Seed database with test data.

This script creates 100 users with various tweets and realistic data.
Run from the project root: python scripts/seed_data.py
"""

import sys
import os
import random
from datetime import datetime, timedelta

# Add parent directory to path to import app modules
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from faker import Faker  # noqa: E402
from twitter_api.app import create_app  # noqa: E402
from twitter_api.database import db  # noqa: E402
from twitter_api.models.user import User  # noqa: E402
from twitter_api.models.tweet import Tweet  # noqa: E402
from twitter_api.models.follow import Follow  # noqa: E402
from twitter_api.utils.password import hash_password  # noqa: E402

fake = Faker()
Faker.seed(42)  # For reproducible data
random.seed(42)


# Tweet templates for more realistic content
TWEET_TEMPLATES = [
    "Just finished {activity}! {emoji}",
    "Can't believe {event}. This is {adjective}!",
    "Hot take: {opinion}",
    "Why does {thing} always happen to me? {emoji}",
    "Loving this {weather} weather today {emoji}",
    "Anyone else {activity}? Just me?",
    "Reminder: {reminder}",
    "{question}?",
    "Life hack: {tip}",
    "Shoutout to everyone who {action}. You're the real MVPs.",
    "{greeting}! Hope everyone has a {adjective} day {emoji}",
    "Currently: {current_activity}",
    "Unpopular opinion: {opinion}",
    "Why is {thing} so {adjective}?",
    "{food} for {meal} today. Living my best life {emoji}",
]

# Content for tweet templates
ACTIVITIES = [
    "reading",
    "coding",
    "working out",
    "cooking",
    "watching a movie",
    "gaming",
    "studying",
    "cleaning",
    "grocery shopping",
    "walking the dog",
]

EVENTS = [
    "it's already Friday",
    "it's Monday again",
    "the year is almost over",
    "summer is here",
    "winter is coming",
    "the weekend flew by",
]

ADJECTIVES = [
    "amazing",
    "awesome",
    "crazy",
    "wild",
    "unbelievable",
    "fantastic",
    "terrible",
    "wonderful",
    "strange",
    "interesting",
    "boring",
    "exciting",
]

OPINIONS = [
    "pineapple belongs on pizza",
    "tabs are better than spaces",
    "the book was better than the movie",
    "coffee is overrated",
    "mornings are the best part of the day",
    "remote work is the future",
]

THINGS = [
    "Mondays",
    "traffic",
    "my alarm",
    "autocorrect",
    "my wifi",
    "printers",
    "updates",
    "meetings",
    "rain",
]

WEATHER_TYPES = ["sunny", "rainy", "snowy", "cloudy", "perfect"]

QUESTIONS = [
    "What's everyone's favorite programming language",
    "Where should I go for lunch",
    "Has anyone seen the latest episode",
    "What's the best way to learn Python",
    "Why do we park in driveways and drive on parkways",
]

REMINDERS = [
    "drink water",
    "take breaks",
    "stretch every hour",
    "back up your files",
    "call your mom",
    "be kind to yourself",
]

TIPS = [
    "use keyboard shortcuts to save time",
    "meal prep on Sundays",
    "set three goals for the day",
    "listen to podcasts while commuting",
    "use a password manager",
]

ACTIONS = [
    "remembered to hydrate today",
    "replied to emails on time",
    "cleaned up their code",
    "asked for help",
    "tried something new",
]

GREETINGS = [
    "Good morning",
    "Happy Monday",
    "Happy Friday",
    "Hey everyone",
    "Morning Twitter",
    "Good evening",
]

FOODS = [
    "Pizza",
    "Tacos",
    "Sushi",
    "Burgers",
    "Salad",
    "Pasta",
    "Ramen",
    "Sandwiches",
    "Soup",
    "Leftovers",
]

MEALS = ["breakfast", "lunch", "dinner", "brunch"]

CURRENT_ACTIVITIES = [
    "debugging",
    "procrastinating",
    "in a meeting",
    "making coffee",
    "thinking about lunch",
    "avoiding work",
    "being productive",
]

EMOJIS = ["😊", "🔥", "💯", "🚀", "❤️", "😂", "🎉", "✨", "🤔", "👀", "💪", "🙌"]


def generate_tweet_content():
    """Generate realistic tweet content."""
    template = random.choice(TWEET_TEMPLATES)

    content = template.format(
        activity=random.choice(ACTIVITIES),
        event=random.choice(EVENTS),
        adjective=random.choice(ADJECTIVES),
        opinion=random.choice(OPINIONS),
        thing=random.choice(THINGS),
        weather=random.choice(WEATHER_TYPES),
        question=random.choice(QUESTIONS),
        reminder=random.choice(REMINDERS),
        tip=random.choice(TIPS),
        action=random.choice(ACTIONS),
        greeting=random.choice(GREETINGS),
        current_activity=random.choice(CURRENT_ACTIVITIES),
        food=random.choice(FOODS),
        meal=random.choice(MEALS),
        emoji=random.choice(EMOJIS),
    )

    # Ensure it's not too long
    if len(content) > 280:
        content = content[:277] + "..."

    return content


def generate_username(first_name, last_name):
    """Generate a realistic username."""
    patterns = [
        f"{first_name.lower()}{last_name.lower()}",
        f"{first_name.lower()}_{last_name.lower()}",
        f"{first_name.lower()}{random.randint(100, 999)}",
        f"{first_name.lower()}.{last_name.lower()}",
        f"{first_name[0].lower()}{last_name.lower()}",
        f"{last_name.lower()}{first_name[0].lower()}",
    ]
    username = random.choice(patterns)

    # Ensure it's not too long
    if len(username) > 50:
        username = username[:50]

    return username


def seed_users(num_users=100):
    """Create seed users."""
    print(f"Creating {num_users} users...")
    users = []

    for i in range(num_users):
        first_name = fake.first_name()
        last_name = fake.last_name()
        username = generate_username(first_name, last_name)

        # Ensure unique username
        base_username = username
        counter = 1
        while User.query.filter_by(username=username).first():
            username = f"{base_username}{counter}"
            counter += 1

        # Some users have display names, some don't
        display_name = f"{first_name} {last_name}" if random.random() > 0.3 else None

        # Some users have bios, some don't
        bio = None
        if random.random() > 0.5:
            interests = ["tech", "coding", "design", "writing", "photography"]
            bio_templates = [
                f"{fake.job()} | {fake.city()}",
                f"Passionate about {random.choice(interests)}",
                fake.catch_phrase(),
                f"{fake.job()} at {fake.company()}",
                " | ".join([fake.bs() for _ in range(2)]),
            ]
            bio = random.choice(bio_templates)
            if len(bio) > 500:
                bio = bio[:497] + "..."

        user = User(
            username=username,
            email=fake.email(),
            # All users have same password for testing
            password_hash=hash_password("password123"),
            display_name=display_name,
            bio=bio,
        )

        db.session.add(user)
        users.append(user)

        if (i + 1) % 20 == 0:
            print(f"  Created {i + 1} users...")

    db.session.commit()
    print(f"✓ Successfully created {num_users} users")
    return users


def seed_tweets(users):
    """Create tweets for users with varied distribution."""
    print(f"Creating tweets for {len(users)} users...")

    total_tweets = 0
    now = datetime.utcnow()

    for i, user in enumerate(users):
        # Varied distribution: some power users, some casual users
        # 20% power users (20-50 tweets)
        # 30% active users (10-19 tweets)
        # 30% moderate users (3-9 tweets)
        # 20% casual users (0-2 tweets)

        rand = random.random()
        if rand < 0.20:
            # Power user
            num_tweets = random.randint(20, 50)
        elif rand < 0.50:
            # Active user
            num_tweets = random.randint(10, 19)
        elif rand < 0.80:
            # Moderate user
            num_tweets = random.randint(3, 9)
        else:
            # Casual user
            num_tweets = random.randint(0, 2)

        # Create tweets with timestamps spread over past 90 days
        for j in range(num_tweets):
            # Random time in the past 90 days
            days_ago = random.uniform(0, 90)
            hours_ago = random.uniform(0, 24)
            created_at = now - timedelta(days=days_ago, hours=hours_ago)

            tweet = Tweet(
                content=generate_tweet_content(),
                user_id=user.id,
                created_at=created_at,
                updated_at=created_at,
            )
            db.session.add(tweet)
            total_tweets += 1

        if (i + 1) % 20 == 0:
            print(f"  Processed {i + 1} users, created {total_tweets} tweets so far...")

    db.session.commit()
    print(f"✓ Successfully created {total_tweets} tweets")
    return total_tweets


def seed_follows(users):
    """Create follow relationships with realistic distribution."""
    print(f"Creating follow relationships for {len(users)} users...")

    total_follows = 0

    for i, user in enumerate(users):
        # Varied follow distribution:
        # 10% influencers (followed by many, follow few): 0-5 following
        # 20% popular users (followed by some, follow some): 10-30 following
        # 40% average users (balanced): 15-40 following
        # 30% new/casual users (follow more than followed): 5-20 following

        rand = random.random()
        if rand < 0.10:
            # Influencer - follow very few
            num_following = random.randint(0, 5)
        elif rand < 0.30:
            # Popular user
            num_following = random.randint(10, 30)
        elif rand < 0.70:
            # Average user
            num_following = random.randint(15, 40)
        else:
            # New/casual user
            num_following = random.randint(5, 20)

        # Select random users to follow (excluding self)
        available_users = [u for u in users if u.id != user.id]
        num_following = min(num_following, len(available_users))

        # Weight selection towards users with more tweets (more active = more discoverable)
        tweet_counts = [Tweet.query.filter_by(user_id=u.id).count() for u in available_users]
        total_tweets_available = sum(tweet_counts) or 1
        weights = [count / total_tweets_available for count in tweet_counts]

        # If all weights are 0, use uniform distribution
        if sum(weights) == 0:
            weights = [1 / len(available_users)] * len(available_users)

        users_to_follow = random.choices(
            available_users, weights=weights, k=num_following
        )

        for followed_user in users_to_follow:
            # Check if already following
            existing = Follow.query.filter_by(
                follower_id=user.id, followed_id=followed_user.id
            ).first()

            if not existing:
                follow = Follow(follower_id=user.id, followed_id=followed_user.id)
                db.session.add(follow)
                total_follows += 1

        if (i + 1) % 20 == 0:
            print(f"  Processed {i + 1} users, created {total_follows} follows so far...")

    db.session.commit()
    print(f"✓ Successfully created {total_follows} follow relationships")
    return total_follows


def print_statistics():
    """Print statistics about seeded data."""
    print("\n" + "=" * 50)
    print("DATABASE STATISTICS")
    print("=" * 50)

    total_users = User.query.count()
    total_tweets = Tweet.query.count()
    total_follows = Follow.query.count()

    print(f"Total Users: {total_users}")
    print(f"Total Tweets: {total_tweets}")
    print(f"Total Follows: {total_follows}")
    print(f"Average Tweets per User: {total_tweets / total_users:.2f}")
    print(f"Average Follows per User: {total_follows / total_users:.2f}")

    # Find most active users
    print("\nMost Active Users:")
    users_with_counts = (
        db.session.query(User, db.func.count(Tweet.id).label("tweet_count"))
        .outerjoin(Tweet)
        .group_by(User.id)
        .order_by(db.func.count(Tweet.id).desc())
        .limit(5)
        .all()
    )

    for user, count in users_with_counts:
        print(f"  @{user.username}: {count} tweets")

    # Find most followed users
    print("\nMost Followed Users:")
    most_followed = (
        db.session.query(User, db.func.count(Follow.follower_id).label("follower_count"))
        .outerjoin(Follow, Follow.followed_id == User.id)
        .group_by(User.id)
        .order_by(db.func.count(Follow.follower_id).desc())
        .limit(5)
        .all()
    )

    for user, count in most_followed:
        print(f"  @{user.username}: {count} followers")

    print("\n" + "=" * 50)
    print("Sample users (username / password):")
    print("=" * 50)
    sample_users = User.query.limit(5).all()
    for user in sample_users:
        print(f"  @{user.username} / password123")

    print("\n✓ Seeding complete!")


def main():
    """Main seeding function."""
    print("=" * 50)
    print("TWITTER API - DATABASE SEEDER")
    print("=" * 50)
    print()

    app = create_app()

    with app.app_context():
        # Check if database already has data
        existing_users = User.query.count()
        if existing_users > 0:
            response = input(
                f"⚠️  Database already has {existing_users} users. "
                "Do you want to continue? This will add more data. (y/N): "
            )
            if response.lower() != "y":
                print("Seeding cancelled.")
                return

        # Create users
        users = seed_users(100)

        # Create tweets
        seed_tweets(users)

        # Create follow relationships
        seed_follows(users)

        # Print statistics
        print_statistics()


if __name__ == "__main__":
    main()
