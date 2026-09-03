# ArtSwipe

ArtSwipe is an Android app for discovering artwork with swipe gestures. Users can like or dislike pieces, build a style profile, get personalized recommendations, and compare art taste with others.

## Features

- Swipe-based artwork discovery feed
- Artwork detail/explanation screen after interactions
- Style profile with personality mapping
- Personalized recommendations based on top styles
- Liked gallery to revisit favorite pieces
- Taste compatibility matching using share codes/deep links (`artswipe://compare?code=...`)
- Firebase-backed authentication and profile persistence

## Tech Stack

- **Language/UI:** Kotlin, Jetpack Compose, Material 3
- **Architecture:** ViewModel + StateFlow, repository pattern, Hilt DI
- **Data:** Room (local cache), Firebase Auth + Firestore
- **Networking:** Retrofit + OkHttp
- **Images:** Coil
- **Art Sources:** The Met Collection API, Art Institute of Chicago API

## Project Structure

```
app/src/main/java/com/artswipe
├── data         # local/remote sources and repository implementations
├── di           # Hilt modules
├── domain       # models, repository interfaces, style logic
└── ui           # navigation, screens, theme
```

## Prerequisites

- Android Studio (recent stable version)
- JDK 11
- Android SDK (minSdk 26, targetSdk 35)
- A Firebase project configured for this app

## Setup

1. Clone the repository.
2. Open the project in Android Studio.
3. Configure Firebase for package `com.artswipe`.
4. Place your Firebase config at:

   `app/google-services.json`

5. Sync Gradle.

## Run

From Android Studio:

- Select the `app` configuration
- Run on an emulator or device

From command line:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Test

```bash
./gradlew test
./gradlew connectedAndroidTest
```
