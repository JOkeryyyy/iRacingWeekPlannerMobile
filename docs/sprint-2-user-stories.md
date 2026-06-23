# Sprint 2 User Stories: Repository, Cache, and Refresh Flow

## Sprint Goal

Implement the data-source, cache, repository, refresh-result, and presentation-state foundations needed before the planner UI MVP.

Sprint 2 stays below the full race-list UI. It should make planner data loadable from local mock JSON, shaped for future hosted JSON, persistable to a local source of truth after a successful load, and representable as presentation-friendly loading, cached, empty, and error states.

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

## Story 2.3: Add SQLDelight Storage for Last Successful Data

**As a developer, I want SQLDelight-backed local storage so the app can keep the latest valid planner data after a successful load or refresh.**

### Acceptance Criteria

- SQLDelight is used as the shared Kotlin Multiplatform local source of truth for planner data.
- The local schema stores a normalized planner dataset for manifest metadata, season, weeks, series, races, race sessions, cars, tracks, and required relationship rows.
- Local storage is written only after the source manifest, season, cars, and tracks decode and map successfully.
- A successful write replaces the planner dataset in one database transaction.
- Invalid source data is not written over a valid local dataset.
- Local reads return a typed data hit, data miss, or local-store failure.
- Tests cover save/load, empty local storage, and "do not overwrite local data on invalid refresh."
- Storage behavior remains in the data layer and does not leak SQLDelight or storage details into domain models.

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

### Current Starting Point

- Story 2.4 has introduced the source-agnostic `PlannerDataSource` contract with local mock and hosted JSON implementations.
- Story 2.3 has introduced SQLDelight-backed local storage for the latest valid planner dataset.
- Story 2.5 should connect those pieces through repository implementations only. It should not add presentation state, Koin app wiring, or planner screen UI.

### Acceptance Criteria

- A shared data-layer refresh/cache coordinator loads from a configured `PlannerDataSource`, maps the source bundle to domain models, and uses `PlannerLocalDataStore` as the local source of truth.
- Concrete data-layer implementations are added for `PlannerScheduleRepository`, `PlannerCarRepository`, and `PlannerTrackRepository`.
- A successful source load that decodes, maps, and saves successfully returns `PlannerDataResult.Loaded` with `PlannerDataFreshness.FRESH`.
- Repository projections return the correct domain slices from one loaded planner dataset:
  - `loadRaceWeeks()` returns season weeks.
  - `loadPlannerRaces()` returns planner races.
  - `loadPlannerCars()` returns planner cars.
  - `loadPlannerTracks()` returns planner tracks.
- A source resource failure or decode failure with a valid stored dataset returns cached data with `PlannerDataFreshness.CACHED`.
- A source resource failure or decode failure with no stored dataset returns a domain-safe error result.
- Invalid required source data after decoding, including mapper validation failures, is never saved over valid stored data.
- Invalid required source data with a valid stored dataset returns cached data with `PlannerDataFreshness.CACHED`.
- Invalid required source data without a stored dataset returns an explicit invalid-source-data error result.
- Local-store read and write failures are represented as explicit domain-safe failures or cached fallback outcomes; repository callers never receive raw SQLDelight, Ktor, serialization, Android, or iOS exceptions.
- If the local-store contract still exposes nullable or Boolean-only outcomes, this story refines the data-layer boundary enough for repositories to distinguish local hit, miss, read failure, write success, and write failure.
- Tests cover fresh success, cache write, cached fallback after source failure, no-cache source failure, invalid-data fallback, invalid-data no-cache failure, local-store failure, and all schedule/car/track repository projections.
- Existing load use cases remain source-agnostic and do not know whether data came from local mock JSON, hosted JSON, or stored data.
- Domain models and repository interfaces remain free of DTOs, Ktor, SQLDelight, Compose, Koin, Android, and iOS APIs.

## Story 2.6: Add Planner Data Presentation State

**As a developer, I want presentation-friendly refresh states so the Sprint 3 planner UI can render loading, cached, empty, and error states cleanly.**

### Current Starting Point

- Story 2.5 is expected to provide source-agnostic repository/use-case results with freshness and domain-safe failures.
- The app currently has only the app-info presentation state holder. Story 2.6 should add planner data presentation state without implementing the planner race-list screen.

### Acceptance Criteria

- A shared presentation state holder or use-case adapter exposes planner data state for Sprint 3 UI work without exposing DTOs, Ktor failures, SQLDelight details, cache rows, or source URLs.
- The state holder uses the domain use cases or a domain-safe planner-data loading facade from Story 2.5; presentation code does not call data sources or local storage directly.
- One UI load action represents one logical planner-data load. It must not trigger separate independent refreshes for weeks, races, cars, and tracks when a single repository/cache result can provide the dataset.
- UI state distinguishes:
  - initial idle or loading state,
  - loaded fresh planner data,
  - loaded cached planner data with a user-presentable refresh warning,
  - empty planner data,
  - refresh/source unavailable error with no cache,
  - invalid required source data with no cache,
  - local-store failure when the app cannot read or save usable data.
- Empty planner data is defined as a successful fresh or cached load with no displayable race weeks or planner races. Missing required fields, malformed timestamps, unknown required session types, and broken relationships are invalid-data states, not empty states.
- Loaded state includes the domain data Sprint 3 needs to build the first planner screen: race weeks, planner races, cars, tracks, freshness, and a concise presentation-safe message or flag when data is cached.
- Error state exposes presentation-safe copy or message keys only. It does not expose raw exception messages as user-facing UI text.
- Tests cover initial/loading transition, fresh loaded data, cached loaded data, empty data, source failure without cache, invalid required data without cache, local-store failure, and retry/reload behavior.
- No planner race-list screen, filtering UI, sorting UI, race detail UI, navigation graph, Firebase sync, account login, or mobile scraping is implemented in this story.
- Presentation depends on domain use cases and presentation state models, not raw network/cache DTOs.

## Story 2.7: Wire DI and Update Development Docs

**As a developer, I want Sprint 2 dependencies wired through Koin and documented so Android/iOS entry points can use the same shared data stack.**

### Current Starting Point

- `commonAppModule` currently wires the app-info repository, app-info use case, and app-info state holder.
- Story 2.7 should wire the completed Sprint 2 data stack after Stories 2.5 and 2.6 are implemented. It should not introduce new repository behavior or presentation states beyond wiring and documentation.

### Acceptance Criteria

- Koin modules register the completed Sprint 2 graph through interfaces where practical:
  - shared JSON configuration,
  - active MVP `PlannerDataSource` backed by local mock resources,
  - SQLDelight-backed `PlannerLocalDataStore`,
  - refresh/cache coordinator,
  - schedule, car, and track repository implementations,
  - planner load/refresh use cases,
  - planner data presentation state holder.
- The active app graph remains local mock-first. Hosted JSON source construction stays available behind the data-layer interface, but no production hosted URL is required and no live network dependency is introduced for the default MVP path.
- Platform-specific SQLDelight driver creation stays in platform source sets or thin platform modules. Domain and presentation code do not create database drivers.
- Platform-specific HTTP client or hosted-source configuration, if wired at all, stays outside domain and is optional until a hosted manifest URL is selected.
- Android and iOS entry points can create shared app dependencies that expose both the existing app-info state holder and the new planner data state holder.
- DI tests verify repository, use-case, refresh/cache coordinator, local data store, and planner state-holder resolution using test-safe dependencies or in-memory drivers.
- DI tests verify the default graph uses the local mock data source rather than the hosted source.
- Existing app-info DI tests remain valid.
- `docs/development.md` lists the new Story 2.5, 2.6, and 2.7 test files, focused test commands, and common Sprint 2 verification commands.
- `docs/development.md` records the Sprint 2 Definition of Done and any local tooling caveats needed for Android host tests, Android debug assembly, or iOS simulator tests.
- No Firebase, account login, cloud sync, mobile scraping, planner race-list screen, filter UI, sorting UI, or race detail UI is introduced by this wiring story.

## Sprint 2 Definition of Done

- Local mock JSON can be loaded through a shared data-source API.
- Hosted JSON loading is represented by a tested source shape without requiring a production endpoint.
- Last successful planner data can be persisted to SQLDelight and read back from the local source of truth.
- Repository implementations return fresh data when available and cached data when refresh fails.
- Invalid required source data is represented as an explicit error state, not silently coerced or dropped.
- Presentation-friendly planner data states exist for Sprint 3 UI work.
- Koin wiring resolves the Sprint 2 data/repository/use-case/state-holder graph.
- Story-focused tests pass.
- Baseline verification passes or has a documented local tooling blocker.

## Verification Plan

Run story-focused tests first, especially mapper/result tests, data-source tests, SQLDelight data-store tests, repository fallback tests, and presentation-state tests.

Common Sprint 2 verification commands:

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```
