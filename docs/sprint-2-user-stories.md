# Sprint 2 User Stories: Repository, Cache, and Refresh Flow

## Sprint Goal

Implement the data-source, cache, repository, refresh-result, and presentation-state foundations needed before the planner UI MVP.

Sprint 2 stays below the full race-list UI. It should make planner data loadable from local mock JSON, shaped for future hosted JSON, cacheable after a successful load, and representable as presentation-friendly loading, cached, empty, and error states.

Sprint 2 should still be implemented story-by-story. Do not combine all data source, cache, repository, refresh, presentation, and DI work into one implementation diff unless explicitly approved.

## Scope Defaults

- Active MVP loading stays local mock-first.
- Hosted JSON is shaped behind interfaces and tests, but does not require a live production URL.
- No mobile scraping, iRacing credentials, Firebase sync, account login, filters, sorting, race details, or planner race-list UI are part of this sprint.
- `docs/roadmap.md` does not need a scope change unless the remote-hosting assumption changes.

## Story 2.1: Add Explicit Data Load Results and Mapper Failure Strategy

**As a developer, I want invalid required source data to become an explicit load failure so the app never crashes or silently hides bad schedule data.**

### Acceptance Criteria

- `PlannerDataResult` and planner data error/freshness types exist in pure domain or presentation-safe shared code.
- Existing planner load use cases return result-wrapped data.
- Mapper behavior no longer coerces invalid required timestamps to epoch.
- Mapper behavior no longer drops unknown required session types.
- Mapper behavior no longer defaults missing required recurring session values to zero.
- Optional fields still map to null or empty values where the contract allows that.
- Tests cover valid mapping, allowed optional omissions, invalid timestamps, invalid session types, and missing required session fields.
- Domain remains free of DTOs, Ktor, Settings, Compose, Koin, Android, and iOS APIs.

## Story 2.2: Add Local Mock JSON Data Source

**As a developer, I want a local mock data source so repositories can load the Sprint 1 fixture contract without a hosted endpoint.**

### Acceptance Criteria

- A data source loads `manifest.json`, `season.json`, `cars.json`, and `tracks.json` from shared mock resources.
- The data source decodes DTOs using the documented mobile JSON contract.
- The source returns an explicit data-source failure instead of throwing raw parsing or resource exceptions to callers.
- Tests verify all four fixture files load and decode through the data-source API.
- The implementation works from shared code and is suitable for Android and iOS callers.

## Story 2.3: Add Cache Storage for Last Successful Data

**As a developer, I want cache storage so the app can keep the latest valid planner data after a successful load or refresh.**

### Acceptance Criteria

- Cache stores the raw JSON bundle for manifest, season, cars, and tracks after all files decode and map successfully.
- Cache reads return a typed cache hit, cache miss, or corrupt-cache failure.
- Invalid source data is not written over a valid cache.
- Tests cover save/load, empty cache, corrupted cache, and "do not overwrite cache on invalid refresh."
- Cache behavior remains in the data layer and does not leak storage details into domain models.

## Story 2.4: Add Hosted JSON Source Shape

**As a developer, I want the remote data source shaped now so hosted JSON can replace local mock data later without changing domain or presentation code.**

### Acceptance Criteria

- A hosted source interface supports loading the mobile data manifest and relative `season.json`, `cars.json`, and `tracks.json` paths.
- A Ktor-backed implementation can be tested with fake or mock HTTP responses.
- No concrete production hosted URL is required.
- No scraping, iRacing credentials, Firebase, or account sync is introduced.
- Domain and presentation code do not depend on hosted-source DTO, URL, or HTTP details.

## Story 2.5: Implement Repository Refresh and Cache Fallback

**As a developer, I want repository implementations to return fresh data when available and cached data when refresh fails.**

### Acceptance Criteria

- Schedule, car, and track repositories are backed by the shared refresh/cache provider.
- Successful local mock load returns usable planner weeks, races, cars, and tracks and writes cache.
- Source failure with valid cache returns cached data with refresh-warning metadata.
- Source failure without cache returns an error result.
- Invalid required source data produces an explicit invalid-data error or cached-with-warning result, never silent coercion.
- Tests cover success, cache fallback, no-cache failure, invalid-data failure, and repository projection for weeks, races, cars, and tracks.
- Use cases remain source-agnostic and do not know whether data came from local mock JSON, hosted JSON, or cache.

## Story 2.6: Add Planner Data Presentation State

**As a developer, I want presentation-friendly refresh states so the Sprint 3 planner UI can render loading, cached, empty, and error states cleanly.**

### Acceptance Criteria

- A shared state holder or use-case adapter exposes planner data UI state without exposing DTOs or cache internals.
- UI state distinguishes loading, loaded fresh, loaded cached with warning, empty data, refresh error, and invalid source data.
- Tests cover normal load, empty planner data, cached fallback, refresh failure without cache, and invalid required data.
- No race-list screen is implemented in this story.
- Presentation depends on domain use cases and presentation state, not raw network/cache DTOs.

## Story 2.7: Wire DI and Update Development Docs

**As a developer, I want Sprint 2 dependencies wired through Koin and documented so Android/iOS entry points can use the same shared data stack.**

### Acceptance Criteria

- `commonAppModule` registers data sources, cache storage, repositories, load/refresh use cases, and planner data state holder through interfaces where practical.
- Platform-specific storage or HTTP configuration stays thin and outside domain.
- DI tests verify repository, use-case, and state-holder resolution.
- `docs/development.md` lists new Sprint 2 test files and verification commands.
- Sprint 2 Definition of Done is documented in this file.

## Sprint 2 Definition of Done

- Local mock JSON can be loaded through a shared data-source API.
- Hosted JSON loading is represented by a tested source shape without requiring a production endpoint.
- Last successful planner JSON can be cached and read back.
- Repository implementations return fresh data when available and cached data when refresh fails.
- Invalid required source data is represented as an explicit error state, not silently coerced or dropped.
- Presentation-friendly planner data states exist for Sprint 3 UI work.
- Koin wiring resolves the Sprint 2 data/repository/use-case/state-holder graph.
- Story-focused tests pass.
- Baseline verification passes or has a documented local tooling blocker.

## Verification Plan

Run story-focused tests first, especially mapper/result tests, data-source tests, cache tests, repository fallback tests, and presentation-state tests.

Common Sprint 2 verification commands:

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```
