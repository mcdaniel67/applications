# Twitter Clone Android App - Planning Document

**Last Updated:** 2025-11-20

---

## Project Overview

Modern Android application for Twitter clone using latest Android development best practices.

### Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt (Dagger)
- **Networking:** Retrofit + OkHttp
- **Async:** Coroutines + Flow
- **Navigation:** Compose Navigation
- **Local Storage:** Room Database
- **Image Loading:** Coil
- **Testing:** JUnit, Mockk, Compose UI Testing

---

## Architecture Layers

### 1. Presentation Layer (UI)
- Jetpack Compose screens
- ViewModels (state management)
- UI state classes
- Navigation graph

### 2. Domain Layer (Business Logic)
- Use cases (single responsibility)
- Domain models
- Repository interfaces

### 3. Data Layer
- Repository implementations
- API service (Retrofit)
- Local database (Room)
- Data models & mappers

---

## Implementation Plan

### Phase 1: Project Setup ✅ COMPLETE
**Goal:** Create base project structure with all dependencies

**Tasks:**
- [x] Create planning document
- [x] Set up Gradle project structure
- [x] Configure build.gradle files (project & app level)
- [x] Add all dependencies (Compose, Hilt, Retrofit, Room, etc.)
- [x] Set up Hilt application class
- [x] Create base package structure
- [x] Configure ProGuard rules
- [x] Set up testing dependencies
- [x] Create gradle wrapper (gradlew, gradlew.bat, gradle-wrapper.jar)
- [x] Create launcher icons (adaptive icons + PNG fallbacks)
- [x] Configure AndroidManifest.xml
- [x] Set up Material 3 theme (Color, Type, Theme)
- [x] Create utility classes (Constants, Result)
- [x] Set up Hilt DI modules (AppModule, NetworkModule)

**Deliverables:**
- ✅ Compilable Android project
- ✅ All dependencies configured
- ✅ Base package structure (presentation, domain, data layers)
- ✅ Gradle wrapper ready
- ✅ Launcher icons created
- ✅ Hilt DI configured
- ✅ Network layer foundation (Retrofit + OkHttp)
- ✅ DataStore for preferences
- ✅ Material 3 theme

---

### Phase 2: Core Infrastructure ✅ COMPLETE
**Goal:** Set up foundational components used across the app

**Tasks:**
- [x] Create API service interface (Retrofit)
- [x] Set up OkHttp client with interceptors
- [x] Create base Response wrapper
- [x] Set up Hilt modules (Network, Database, Repository)
- [x] Create sealed Result class for API responses
- [x] Create domain models and mappers
- [x] Set up navigation structure
- [x] Create PreferencesManager for token storage
- [x] Implement AuthRepository

**Deliverables:**
- ✅ Network layer ready (ApiService, AuthInterceptor)
- ✅ DI configured (RepositoryModule)
- ✅ Navigation routes defined
- ✅ Auth repository implemented

---

### Phase 3: Authentication Flow ✅ COMPLETE
**Goal:** User can register, login, and persist auth state

**Features:**
- ✅ Login screen
- ✅ Registration screen
- ✅ JWT token storage (DataStore)
- ✅ Auto-login on app start
- ✅ Logout functionality

**Tasks:**
- [x] Create LoginViewModel and LoginScreen
- [x] Create RegisterViewModel and RegisterScreen
- [x] Set up NavGraph with auth flow
- [x] Integrate navigation in MainActivity
- [x] Implement auto-login logic
- [x] Add loading states and error handling

**Deliverables:**
- ✅ Complete authentication UI
- ✅ Token persistence
- ✅ Navigation between auth screens
- ✅ Auto-login functionality

---

**Tasks:**
- [ ] Create User domain model
- [ ] Create AuthRepository interface & implementation
- [ ] Create LoginUseCase, RegisterUseCase, LogoutUseCase
- [ ] Create AuthViewModel
- [ ] Build Login screen (Compose)
- [ ] Build Registration screen (Compose)
- [ ] Implement token storage (DataStore)
- [ ] Add auth interceptor for API calls
- [ ] Handle token expiration
- [ ] Write unit tests for auth logic
- [ ] Write UI tests for auth screens

**API Endpoints Used:**
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/logout

---

### Phase 4: Home Feed ✅ COMPLETE
**Goal:** Display personalized tweet feed

**Features:**
- ✅ Tweet feed with LazyColumn
- ✅ Global feed (all tweets)
- ✅ Following feed (tweets from followed users)
- ✅ Tab navigation between feeds
- ✅ Relative timestamps (e.g., "5m ago", "2h ago")
- ✅ Loading and empty states
- ✅ Error handling with Snackbar
- ✅ Logout functionality

**Tasks:**
- [x] Create TweetRepository
- [x] Create FeedViewModel
- [x] Build FeedScreen with tabs
- [x] Create TweetItem composable
- [x] Add TimeUtils for timestamps
- [x] Configure API URL via local.properties
- [x] Handle nullable API responses

**Deliverables:**
- ✅ Functional tweet feed
- ✅ Tab switching
- ✅ Formatted timestamps
- ✅ Error handling

---
- Tweet cards with user info
- Timestamp formatting
- Empty state handling

**Tasks:**
- [ ] Create Tweet domain model
- [ ] Create FeedRepository interface & implementation
- [ ] Create GetFeedUseCase
- [ ] Create FeedViewModel with pagination
- [ ] Build Feed screen (Compose)
- [ ] Create TweetCard composable
- [ ] Implement pull-to-refresh
- [ ] Implement infinite scroll (LazyColumn)
- [ ] Add loading states
- [ ] Add error handling
- [ ] Write unit tests
- [ ] Write UI tests

**API Endpoints Used:**
- GET /api/feed

---

### Phase 5: Tweet Creation
**Goal:** User can create new tweets

**Features:**
- Tweet compose screen
- Character counter (280 max)
- Post button (enabled when valid)
- Loading state during post
- Success/error feedback

**Tasks:**
- [ ] Create CreateTweetUseCase
- [ ] Create ComposeTweetViewModel
- [ ] Build ComposeTweet screen
- [ ] Add character counter
- [ ] Implement validation
- [ ] Handle post success/failure
- [ ] Navigate back on success
- [ ] Write unit tests
- [ ] Write UI tests

**API Endpoints Used:**
- POST /api/tweets

---

### Phase 6: User Profiles
**Goal:** View user profiles with their tweets

**Features:**
- User profile header (avatar, bio, stats)
- User's tweet list
- Follow/unfollow button
- Edit own profile

**Tasks:**
- [ ] Create GetUserProfileUseCase
- [ ] Create GetUserTweetsUseCase
- [ ] Create FollowUserUseCase, UnfollowUserUseCase
- [ ] Create ProfileViewModel
- [ ] Build Profile screen
- [ ] Create ProfileHeader composable
- [ ] Implement follow/unfollow
- [ ] Build EditProfile screen
- [ ] Write unit tests
- [ ] Write UI tests

**API Endpoints Used:**
- GET /api/users/:id
- GET /api/users/:id/tweets
- POST /api/users/:id/follow
- DELETE /api/users/:id/follow
- PUT /api/users/:id

---

### Phase 7: Global Feed & Discovery
**Goal:** Browse all tweets and discover users

**Features:**
- Global feed tab
- User search
- Trending/popular content

**Tasks:**
- [ ] Create GetGlobalFeedUseCase
- [ ] Create GlobalFeedViewModel
- [ ] Build GlobalFeed screen
- [ ] Add tab navigation (Home/Global)
- [ ] Implement search functionality
- [ ] Write unit tests
- [ ] Write UI tests

**API Endpoints Used:**
- GET /api/feed/global
- GET /api/users

---

### Phase 8: Social Features
**Goal:** Likes, retweets, replies (when API ready)

**Features:**
- Like/unlike tweets
- Retweet functionality
- Reply to tweets
- View tweet details with replies

**Tasks:**
- [ ] TBD - depends on backend API

---

### Phase 9: Polish & Optimization
**Goal:** Production-ready app

**Tasks:**
- [ ] Add app icon & splash screen
- [ ] Implement proper error handling
- [ ] Add analytics (optional)
- [ ] Optimize performance
- [ ] Add accessibility labels
- [ ] Test on multiple devices
- [ ] Handle edge cases
- [ ] Add ProGuard rules
- [ ] Prepare for release

---

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/twitterclone/
│   │   │   ├── TwitterApplication.kt
│   │   │   ├── di/                    # Hilt modules
│   │   │   │   ├── AppModule.kt
│   │   │   │   ├── NetworkModule.kt
│   │   │   │   ├── DatabaseModule.kt
│   │   │   │   └── RepositoryModule.kt
│   │   │   ├── data/                  # Data layer
│   │   │   │   ├── local/            # Room database
│   │   │   │   │   ├── dao/
│   │   │   │   │   ├── entities/
│   │   │   │   │   └── TwitterDatabase.kt
│   │   │   │   ├── remote/           # API
│   │   │   │   │   ├── api/
│   │   │   │   │   ├── dto/
│   │   │   │   │   └── interceptors/
│   │   │   │   ├── repository/       # Repository implementations
│   │   │   │   └── mapper/           # DTO to Domain mappers
│   │   │   ├── domain/                # Domain layer
│   │   │   │   ├── model/            # Domain models
│   │   │   │   ├── repository/       # Repository interfaces
│   │   │   │   └── usecase/          # Use cases
│   │   │   │       ├── auth/
│   │   │   │       ├── tweet/
│   │   │   │       ├── user/
│   │   │   │       └── feed/
│   │   │   ├── presentation/          # UI layer
│   │   │   │   ├── theme/            # Material 3 theme
│   │   │   │   ├── navigation/       # Navigation graph
│   │   │   │   ├── components/       # Reusable composables
│   │   │   │   ├── auth/             # Auth screens
│   │   │   │   │   ├── login/
│   │   │   │   │   └── register/
│   │   │   │   ├── feed/             # Feed screens
│   │   │   │   ├── tweet/            # Tweet screens
│   │   │   │   ├── profile/          # Profile screens
│   │   │   │   └── common/           # Common UI components
│   │   │   └── util/                  # Utilities
│   │   │       ├── Constants.kt
│   │   │       ├── Extensions.kt
│   │   │       └── Result.kt
│   │   └── res/                       # Resources
│   │       ├── drawable/
│   │       ├── values/
│   │       └── xml/
│   ├── test/                          # Unit tests
│   │   └── java/com/example/twitterclone/
│   │       ├── domain/usecase/
│   │       ├── data/repository/
│   │       └── presentation/viewmodel/
│   └── androidTest/                   # Integration tests
│       └── java/com/example/twitterclone/
│           ├── ui/
│           └── data/
├── build.gradle.kts
└── proguard-rules.pro
```

---

## Key Dependencies

### Core
```kotlin
// Kotlin
kotlin("android") version "1.9.20"
kotlin("kapt") version "1.9.20"

// Android
compileSdk = 34
minSdk = 24
targetSdk = 34
```

### Jetpack Compose
```kotlin
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.compose.ui:ui-tooling-preview
androidx.activity:activity-compose
androidx.lifecycle:lifecycle-viewmodel-compose
androidx.navigation:navigation-compose
```

### Dependency Injection
```kotlin
com.google.dagger:hilt-android
com.google.dagger:hilt-compiler
androidx.hilt:hilt-navigation-compose
```

### Networking
```kotlin
com.squareup.retrofit2:retrofit
com.squareup.retrofit2:converter-gson
com.squareup.okhttp3:okhttp
com.squareup.okhttp3:logging-interceptor
```

### Database
```kotlin
androidx.room:room-runtime
androidx.room:room-ktx
androidx.room:room-compiler
```

### Async
```kotlin
org.jetbrains.kotlinx:kotlinx-coroutines-android
org.jetbrains.kotlinx:kotlinx-coroutines-core
```

### Storage
```kotlin
androidx.datastore:datastore-preferences
```

### Image Loading
```kotlin
io.coil-kt:coil-compose
```

### Testing
```kotlin
junit:junit
io.mockk:mockk
org.jetbrains.kotlinx:kotlinx-coroutines-test
androidx.compose.ui:ui-test-junit4
androidx.test.ext:junit
androidx.test.espresso:espresso-core
```

---

## API Integration

### Base URL
```
http://10.0.2.2:5000/api  # Android emulator localhost
```

### Authentication
- JWT token stored in encrypted DataStore
- Token added to all requests via OkHttp interceptor
- Token refresh on 401 responses

### Error Handling
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

---

## Testing Strategy

### Unit Tests
- ViewModels: Test state changes and business logic
- Use Cases: Test business rules
- Repositories: Test data mapping and caching
- Use Mockk for mocking
- Use Coroutines TestDispatcher

### Integration Tests
- API integration tests
- Database tests
- Repository tests with real Room database

### UI Tests
- Compose UI tests for each screen
- Navigation tests
- User flow tests (login → feed → create tweet)

---

## Current Status

**Phase:** 1 - Project Setup
**Progress:** Planning document created
**Next Step:** Create Gradle project structure

---

## Notes & Decisions

### Why Clean Architecture?
- Separation of concerns
- Testability
- Scalability
- Independent of frameworks

### Why Jetpack Compose?
- Modern declarative UI
- Less boilerplate
- Better performance
- Official recommendation from Google

### Why Hilt?
- Official DI solution for Android
- Better integration with Android components
- Compile-time safety

### Why Room?
- Offline-first capability
- Cache API responses
- Better user experience

---

## Future Enhancements

- [ ] Dark mode support
- [ ] Push notifications
- [ ] Image upload for tweets
- [ ] Video support
- [ ] Direct messages
- [ ] Hashtag support
- [ ] Mentions
- [ ] Search functionality
- [ ] Bookmarks
- [ ] Lists
- [ ] Spaces (audio rooms)

---

## Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Material 3 Design](https://m3.material.io/)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
