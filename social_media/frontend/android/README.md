# Twitter Clone Android App

Modern Android application built with Jetpack Compose and Clean Architecture.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (Dagger)
- **Networking**: Retrofit + OkHttp
- **Async**: Coroutines + Flow
- **Navigation**: Compose Navigation
- **Storage**: DataStore Preferences
- **Build**: Gradle 8.11.1, AGP 8.7.3, Kotlin 2.1.0

## Build Variants

The app supports different backend URLs for debug and release builds:

### Debug Build
- **API URL**: `http://10.0.2.2:5000/api/`
- Points to localhost:5000 (Android emulator mapping)
- Use this for local development

### Release Build
- **API URL**: `https://your-production-api.com/api/`
- Configure in `app/build.gradle.kts` before release

## Building

```bash
# Debug build (points to localhost:5000)
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on device/emulator
./gradlew installDebug

# Run tests
./gradlew test
```

## Running with Local Backend

1. Start the backend API on your machine:
   ```bash
   cd twitter-api
   docker compose up
   ```

2. The backend will be available at `localhost:5000`

3. Build and install the debug APK:
   ```bash
   ./gradlew installDebug
   ```

4. The app will automatically connect to `http://10.0.2.2:5000/api/` (which maps to your localhost)

## Project Structure

```
app/src/main/java/com/example/twitterclone/
├── data/
│   ├── local/          # DataStore, preferences
│   ├── mapper/         # DTO to domain mappers
│   ├── remote/         # API service, DTOs
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models
│   └── repository/     # Repository interfaces
├── presentation/
│   ├── auth/           # Login, Register screens
│   ├── feed/           # Feed screen
│   ├── navigation/     # Navigation graph
│   └── theme/          # Material 3 theme
├── di/                 # Hilt modules
└── util/               # Constants, Result class
```

## Features Implemented

### Phase 1: Project Setup ✅
- Complete Gradle configuration
- All dependencies
- Hilt DI setup
- Material 3 theme
- Launcher icons

### Phase 2: Core Infrastructure ✅
- API service with all endpoints
- Auth interceptor for JWT tokens
- DataStore for token storage
- Repository pattern
- Domain models and mappers

### Phase 3: Authentication ✅
- Login screen
- Registration screen
- Auto-login on app start
- Token persistence
- Navigation flow

### Phase 4: Tweet Feed ✅
- Global feed (all tweets)
- Following feed (tweets from followed users)
- Tab navigation
- Relative timestamps ("5m ago", "2h ago")
- Loading and empty states
- Error handling with Snackbar
- Logout functionality

## Configuration

### Setting API URL

The API URL is configured in `local.properties` (not tracked in git):

```properties
# For Android emulator
debug.api.url=http://10.0.2.2:5000/api/

# For physical device (use your machine's IP)
debug.api.url=http://192.168.1.100:5000/api/

# Production
release.api.url=https://your-production-api.com/api/
```

**First time setup:**
1. Copy `local.properties.example` to `local.properties`
2. Customize the URLs for your environment
3. Build the app

The file is gitignored, so your local settings won't be committed.

### Environment Variables (Alternative)

You can also use environment variables:

```bash
export DEBUG_API_URL="http://10.0.2.2:5000/api/"
./gradlew assembleDebug
```

Then update `app/build.gradle.kts` to read from env vars.

## Requirements

- Android Studio Ladybug or later
- JDK 21
- Android SDK 35
- Minimum Android 7.0 (API 24)

## Next Steps

- [ ] Phase 4: Tweet Feed UI
- [ ] Phase 5: Tweet Creation
- [ ] Phase 6: User Profiles
- [ ] Phase 7: Follow/Unfollow UI
- [ ] Phase 8: Polish & Testing
