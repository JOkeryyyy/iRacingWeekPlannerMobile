# Requirements

## Functional Requirements

### Planner

- Show a list of races for the selected week or date.
- Allow the user to move between race weeks.
- Allow sorting by useful planner fields such as time, series, car, track, or participation priority.
- Allow filtering by car and track ownership.
- Allow filtering or highlighting by favorite cars and tracks.
- Open a race detail view from the list.

### Local Preferences

- Store owned cars locally.
- Store owned tracks locally.
- Store favorite cars locally.
- Store favorite tracks locally.
- Preferences are device-local in v1.
- Preferences do not require login.

### Data Loading

- MVP starts with local mock JSON fixtures.
- The app should later consume hosted generated JSON without changing domain or presentation code.
- Hosted data files are expected to be:
  - `manifest.json`
  - `season.json`
  - `cars.json`
  - `tracks.json`
- The app should cache the latest successful data load.
- After the first successful data load, the app should work offline with cached data.
- Data refresh failures should preserve the last usable cache.

### Platform

- Android app must run on a common/popular Android configuration.
- iOS app must run on a common/popular iOS simulator/device configuration.
- Shared business logic should live in Kotlin Multiplatform shared code.
- UI should be built with Compose Multiplatform where practical.

## Quality Requirements

- Domain logic should have unit tests.
- JSON parsing should have serialization tests.
- DTO-to-domain mapping should have mapper tests.
- Repository refresh/cache behavior should have tests before beta.
- State holders/ViewModels should have tests for loading, loaded, empty, and error states.

## Constraints

- No iRacing scraping in the mobile app.
- No iRacing credentials in the mobile app.
- No Firebase login or settings sync in v1.
- Keep Clean Architecture boundaries visible and enforceable.
- Keep docs updated as scope and behavior change.
