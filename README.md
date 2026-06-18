# TeamManager

An Android app for amateur football teams to manage match attendance. Players confirm whether they're coming to a match, and the count updates live across every device — so the coach gets a real-time overview instead of chasing replies in WhatsApp.

**Author:** Yevhenii Lebediev (58798)
**Repository:** https://github.com/evgen06ua/team-manager-app

---

## Problem

Amateur teams struggle with no-shows because players have no easy, shared way to confirm attendance. Availability gets scattered across separate chat messages and there's no single overview of who is actually coming to a given match.

## Solution

TeamManager gives the team one place to:

- Create upcoming matches (opponent, location, date).
- Let each player tap **I'm coming** / **Not coming** per match.
- See a **live count** of who's coming and who's out, synced instantly to every device.
- See how many teammates are **online right now**.

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Min SDK:** API 24
- **Backend:** Firebase (Spark plan)
  - Authentication (email / password)
  - Cloud Firestore (matches)
  - Realtime Database (live availability + presence)
- **Testing:** JUnit (local unit tests)

---

## Features

### Authentication
- Register and log in with email / password.
- Session check on startup — logged-in users skip the login screen.
- Input validation with clear error messages.

### Matches (Firestore CRUD)
- Real-time match list backed by a Firestore snapshot listener.
- Add a match (opponent, location, date) with required-field validation.
- Delete a match with a confirmation dialog.

### Live Availability & Presence (Realtime Database)
- Each match card shows a **live counter** of players coming vs. out.
- Tapping a button writes to `availability/{matchId}/{userId}`; a `ValueEventListener` re-renders the counts on every device in milliseconds — no refresh.
- **Presence:** users are marked online on entry and automatically offline via `onDisconnect()` when the app closes or loses connection. The home screen shows a live "online now" count.

### Testing
- Core validation and business logic extracted into `ValidationUtils` and covered by 8 passing JUnit tests (email format, password length, match-form validation, availability counting).

---

## Data Model

### Firestore

**`users`**
| Field | Type | Notes |
|-------|------|-------|
| userId | string | auto |
| email | string | |
| displayName | string | |
| role | string | `"player"` or `"coach"` (defined; not yet enforced in UI) |

**`matches`**
| Field | Type |
|-------|------|
| matchId | string (auto) |
| opponent | string |
| location | string |
| matchDate | string |
| status | string (`"upcoming"`) |
| createdBy | string (userId) |

### Realtime Database

```
availability/
  {matchId}/
    {userId}: "coming" | "notcoming"
presence/
  {userId}: true | false
```

---

## Setup

1. **Clone**
   ```bash
   git clone https://github.com/evgen06ua/team-manager-app.git
   ```
2. **Firebase project** — create a Firebase project (or use an existing one) and register an Android app with package `com.example.teammanager`.
3. **Enable services** in the Firebase Console:
   - Authentication → Email/Password
   - Firestore Database (test mode for development)
   - Realtime Database (test mode; pick the region closest to you)
4. **Add `google-services.json`** to the `app/` folder.
   > **Note:** download `google-services.json` *after* enabling Realtime Database so it includes the database URL. For non-US regions (e.g. `europe-west1`) the URL is required, otherwise the database calls fail silently.
5. **Run** the app from Android Studio on an emulator or device.

---

## Running the Tests

In Android Studio, open `ValidationUtilsTest` and click the green run arrow next to the class → **Run 'ValidationUtilsTest'** (all 8 tests run).

Or from the project root:

```bash
gradlew test          # Windows
./gradlew test        # macOS / Linux
```

Reports are written to `app/build/reports/tests/`.

---

## Project Management

- **Version control:** GitHub, with feature work merged via pull requests.
- **Task tracking:** Trello board (Backlog → Sprint columns → Done), updated per sprint.

---

## Future Improvements

- Show *which* players are coming (names per match), not just counts.
- Scope the "online now" count to the team rather than all app users.
- Replace the free-text match date with a date/time picker and validation.
- Add a "Maybe" availability option (already supported by the data model).
- Enforce roles: restrict match deletion to the coach / match creator.

---


