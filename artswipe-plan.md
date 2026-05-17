# ArtSwipe — App Plan

## Overview

A Tinder-style art discovery app. Users swipe on artworks, learn what styles they gravitate toward, and build a taste profile over time. Content pulls from public domain museum APIs. User data is saved to a backend account.

---

## Tech Stack

| Layer | Choice | Reason |
|---|---|---|
| Language | Kotlin | Standard for Android |
| UI | Jetpack Compose | Modern, Gemini-friendly |
| Auth | Firebase Auth | Email + Google sign-in |
| Database | Firestore | Syncs swipe history + profile across devices |
| Local Cache | Room | Offline support, avoids redundant API calls |
| Networking | Retrofit + OkHttp | Standard, well-documented |
| Image Loading | Coil | Compose-native |
| Art APIs | Met Museum API, Art Institute of Chicago API | Free, no key required, high quality |

---

## Branch Structure

```
main
└── develop
    ├── feature/project-setup
    ├── feature/auth
    ├── feature/artwork-api
    ├── feature/swipe-ui
    ├── feature/explanation-screen
    ├── feature/style-engine
    ├── feature/profile-screen
    ├── feature/recommendations
    ├── feature/liked-gallery
    └── feature/compatibility
```

Each feature branch merges into `develop`. Once stable, `develop` merges into `main`.

---

## Branch Details

### `feature/project-setup`
Set up the base project before any feature work.

**Tasks:**
- Create Android project with Kotlin + Compose
- Add all dependencies to `build.gradle`
- Set up package structure: `ui/`, `data/`, `domain/`, `di/`
- Configure Firebase (Auth + Firestore)
- Add Room database scaffold
- Set up Retrofit client
- Create base `ViewModel` and `Repository` interfaces
- Set up navigation graph with placeholder screens

**Deliverable:** Compiles and runs with empty nav structure.

---

### `feature/auth`
Handle sign-up, login, and session persistence.

**Tasks:**
- Splash screen that checks for existing session
- Sign-up screen (email/password + Google)
- Login screen
- Password reset flow
- On first login, create a Firestore user document
- On return, load existing profile
- Sign-out from settings

**Firestore user document structure:**
```json
{
  "userId": "uid_abc123",
  "displayName": "Jane",
  "joinDate": "2025-01-01",
  "totalSwipes": 0,
  "styleScores": {},
  "shareCode": "ART-4X9K"
}
```

**Deliverable:** Full auth flow working, user doc created on first sign-in.

---

### `feature/artwork-api`
Pull and normalize artwork data from two public APIs.

**APIs:**
- **Met Museum API** — `https://collectionapi.metmuseum.org/public/collection/v1/`
  - `/search?q=impressionism&hasImages=true` to find object IDs
  - `/objects/{id}` to get full artwork data
- **Art Institute of Chicago API** — `https://api.artic.edu/api/v1/artworks`
  - Supports filtering by style, medium, department

**Tasks:**
- Create `ArtworkRepository` with methods for both APIs
- Normalize both sources into a single `Artwork` data class
- Cache results in Room to avoid repeat fetches
- Build a seeding function that pre-fetches a queue of 20+ artworks on app launch
- Tag each artwork with a `styleMovement` field (Impressionism, Baroque, Modernism, etc.)
- Filter out artworks without images

**Artwork data model:**
```kotlin
data class Artwork(
    val id: String,
    val source: String,         // "met" or "aic"
    val title: String,
    val artist: String,
    val year: String?,
    val imageUrl: String,
    val styleMovement: String,  // e.g. "Impressionism"
    val medium: String?,
    val description: String,    // shown post-swipe
    val department: String?
)
```

**Deliverable:** Can fetch, normalize, and cache 20+ artworks from both APIs.

---

### `feature/swipe-ui`
The main screen. Card stack with swipe gestures.

**Tasks:**
- Build a swipeable card stack (top card is interactive, next card previews behind it)
- Left swipe = dislike, right swipe = like
- Overlay indicator: green heart on right drag, red X on left drag
- On swipe complete, save result to Firestore + local Room DB
- Load next card from cached queue; trigger background refetch when queue drops below 5
- Show title and artist name at the bottom of the card upfront
- After swipe, navigate to explanation screen before returning to next card

**Swipe record data model:**
```kotlin
data class SwipeRecord(
    val userId: String,
    val artworkId: String,
    val liked: Boolean,
    val timestamp: Long,
    val styleMovement: String
)
```

**Deliverable:** Fully working swipe deck that saves results.

---

### `feature/explanation-screen`
Show artwork context after each swipe.

**Tasks:**
- Full-screen modal or bottom sheet that appears after swipe
- Show: artwork image, title, artist, year, medium, style movement
- Show a 2-3 sentence description of the artwork
- Show a 2-3 sentence description of the style movement (not the specific piece)
- Show whether the user liked or disliked it (subtle indicator)
- "Continue" button to dismiss and return to swipe deck

**Deliverable:** Explanation screen renders correctly for liked and disliked swipes.

---

### `feature/style-engine`
Compute and update the user's style profile from swipe history.

**Tasks:**
- On each swipe, update `styleScores` in the user's Firestore doc
  - Liked: +2 to that style
  - Disliked: -1 to that style
- Calculate percentage breakdown from scores (normalize to 100%)
- Determine top style (highest score)
- Map top style to a personality card label (see below)
- Expose a `StyleProfileState` from a `ViewModel` for the profile screen

**Style to personality label map (examples):**
| Style | Personality Card |
|---|---|
| Impressionism | "The Dreamer" |
| Baroque | "The Dramatist" |
| Modernism | "The Visionary" |
| Surrealism | "The Wanderer" |
| Realism | "The Grounded" |
| Abstract | "The Free Spirit" |
| Renaissance | "The Classicist" |

**Deliverable:** Style scores update live; profile computes correctly after 10+ swipes.

---

### `feature/profile-screen`
Display the user's evolving taste profile.

**Three sections on one screen:**

1. **Personality Card**
   - Full-width card with label (e.g. "The Dreamer"), top style name, and a short 1-sentence description
   - Only shows after 10+ swipes (shows "Keep swiping" prompt before that)

2. **Style Breakdown**
   - Horizontal bar chart or segmented breakdown
   - Shows top 5 styles with percentage of likes
   - Tapping a style shows a brief description of that movement

3. **Top Styles List**
   - Simple ranked list: #1, #2, #3...
   - Each row shows style name, like count, and a thumbnail of a liked artwork from that style

**Deliverable:** Profile screen renders all three sections, updates after each swipe session.

---

### `feature/recommendations`
Surface artworks the user is likely to enjoy based on their profile.

**Tasks:**
- Query the artwork API filtered by the user's top 2-3 styles
- Exclude already-seen artwork IDs (stored in Room)
- Display as a vertical scroll feed (not swipeable — separate from the main deck)
- Each card shows image, title, artist, style tag
- Tapping opens the explanation screen
- Add a "Buy / Explore" external link if the artwork has a source URL (both APIs provide these)

**Deliverable:** Recommendations feed populates based on swipe history.

---

### `feature/liked-gallery`
Let users revisit artworks they liked.

**Tasks:**
- Grid view of all liked artworks, newest first
- Tap to open explanation screen
- Filter by style movement (pill filters at top)
- Show total liked count
- Option to unlike from this screen (updates Firestore + recalculates style scores)

**Deliverable:** Gallery renders liked artworks with filtering and unlike support.

---

### `feature/compatibility`
Let users compare their art taste with anyone via a shareable link or code.

**How it works:**
- Every user gets a unique short code on account creation (e.g. `ART-4X9K`), stored in their Firestore doc
- User shares their code or a deep link: `artswipe://compare?code=ART-4X9K`
- Recipient enters the code or taps the link → app fetches that user's public style profile and runs the compatibility calculation
- No friend list, no follow system — stateless comparison, anyone with the code can view

**Tasks:**
- On account creation, generate and store a unique `shareCode` (see auth branch update above)
- Build a Firestore query to look up a user by `shareCode`
- Add a "Compare Tastes" entry point on the Profile screen
- Build the share sheet: copy code + share deep link via Android share intent
- Build the code entry screen (manual input fallback)
- Handle deep link routing in the nav graph
- Fetch the other user's `styleScores` (read-only, public field)
- Run the compatibility algorithm (see below)
- Build the compatibility result screen

**What to make public on each user's Firestore doc:**
- `displayName`
- `styleScores`
- `shareCode`
- `totalSwipes` (to show "based on X swipes" disclaimer)

Do not expose swipe history, email, or auth details.

**Compatibility algorithm:**

Normalize both users' `styleScores` into percentage distributions, then compute overlap:

```kotlin
fun computeCompatibility(
    scoresA: Map<String, Int>,
    scoresB: Map<String, Int>
): Float {
    val allStyles = scoresA.keys + scoresB.keys
    val totalA = scoresA.values.sum().toFloat()
    val totalB = scoresB.values.sum().toFloat()

    var overlap = 0f
    for (style in allStyles) {
        val pctA = (scoresA[style] ?: 0) / totalA
        val pctB = (scoresB[style] ?: 0) / totalB
        overlap += minOf(pctA, pctB)
    }
    return overlap * 100f  // returns 0–100
}
```

This is a distribution overlap score. 100% means identical taste distribution, 0% means no overlap at all.

**Compatibility label map:**
| Score | Label |
|---|---|
| 85–100% | "Kindred Spirits" |
| 65–84% | "Fellow Admirers" |
| 45–64% | "Curious Contrast" |
| 25–44% | "Worlds Apart" |
| 0–24% | "Total Opposites" |

**Compatibility result screen — three sections:**

1. **Match Card**
   - Large % score front and center
   - Compatibility label below (e.g. "Kindred Spirits")
   - Both users' personality card labels side by side (e.g. "The Dreamer × The Dramatist")

2. **Shared Styles**
   - List of styles both users scored positively on, ranked by mutual strength
   - Shows each style's % contribution to both profiles side by side

3. **Where You Differ**
   - Top style unique to each user that the other didn't respond to
   - Framed as interesting contrast, not a negative

**Edge cases to handle:**
- Other user has fewer than 10 swipes → show result with a "Limited data — scores may shift" disclaimer
- Code not found → clear error state with retry
- User tries to compare with themselves → show a friendly message

**Deliverable:** Share flow works end-to-end. Compatibility result screen renders all three sections with real data.

---



```
Splash
└── Auth (if no session)
    ├── Sign Up
    └── Login

Main App (bottom nav: Discover / Profile / Liked)
├── Discover (Swipe Deck)
│   └── Explanation Screen (after each swipe)
├── Profile
│   ├── Personality Card
│   ├── Style Breakdown
│   ├── Top Styles List
│   │   └── Recommendations Feed
│   └── Compare Tastes
│       ├── Share Code / Link Sheet
│       ├── Enter Code Screen
│       └── Compatibility Result Screen
└── Liked Gallery
    └── Explanation Screen (on tap)

Settings (from Profile)
└── Sign Out / Account
```

---

## Development Order

Build in this order to keep each branch unblocked:

1. `project-setup`
2. `auth`
3. `artwork-api`
4. `swipe-ui`
5. `explanation-screen`
6. `style-engine`
7. `profile-screen`
8. `liked-gallery`
9. `recommendations`
10. `compatibility`

---

## Notes for Gemini

- Use Jetpack Compose throughout. No XML layouts.
- Use `StateFlow` + `ViewModel` for all UI state.
- All Firestore writes should be fire-and-forget with error logging — don't block UI.
- Room is the source of truth for the swipe queue. Firestore is for persistence + cross-device sync.
- The Met API does not require an auth key. The AIC API also does not. Both are free.
- Start seeding from specific department/classification filters to get style diversity (e.g. Met: `department=11` for European Paintings).
- Target API level 26+ (Android 8.0).
- For deep links (`artswipe://compare?code=...`), register the scheme in `AndroidManifest.xml` and handle it in the nav graph using `NavDeepLink`.
- The `styleScores` field on each Firestore user doc should be readable without auth (public read, private write via Firestore rules) to support the stateless compatibility lookup.
