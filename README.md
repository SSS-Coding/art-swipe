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
- JDK 17 or newer to run Gradle (the app targets Java 11 bytecode)
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

## Taste scoring

Compatibility compares the proportion of liked artwork in each known style. The
sum of the smaller proportion for each style gives a symmetric overlap score from
0 to 100. Identical distributions score 100, even with different history sizes;
disjoint distributions score 0. Shared passes are displayed separately and add no
bonus points. Missing likes produce an unscored state, and either person having
fewer than 10 swipes triggers an early-data notice.

Existing Firestore data uses `styleScores = 2 * likes - dislikes`. The app recovers
like counts with `(styleScores + styleDislikes) / 2`, so both profiles and comparisons
use actual likes without a migration. Style names are normalized for whitespace
and case; blank, unknown and unclassified styles do not affect taste calculations.
Previously misclassified museum records are not automatically rewritten.

## Verification

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Unit tests cover scoring bounds, symmetry, negative scores, missing data, count
overflow, style normalization, and share codes/links. Emulator tests cover profile
loading, comparison UI, dark mode, account-isolated Room queries, duplicate legacy
records, collection ordering, and stable cached metadata. The comparison UI tests
use fake profiles and do not write to Firebase.

Firestore swipe writes use atomic batches and retain offline queuing. This does
not implement cross-device conflict resolution or a durable Room-to-Firestore
outbox: permanent remote write failures are logged. Firebase authentication,
security rules and real two-account sharing still need validation against the
configured Firebase project before release.
