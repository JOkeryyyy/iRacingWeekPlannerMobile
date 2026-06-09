# Roadmap

## Sprint 0: KMP/Compose Project Setup and Technical Spike

Goal: Prove the mobile workspace can build for Android and iOS, establish Clean Architecture boundaries, add baseline dependencies, and document local setup.

Outcomes:

- Dedicated mobile repo is the project home.
- KMP shared module exists.
- Android target is runnable or has a documented blocker.
- iOS target is runnable or has a documented blocker.
- Shared smoke tests exist.
- Clean Architecture package boundaries are present.
- Local setup and verification commands are documented.

## Sprint 1: Data Contract and Domain Foundation

Goal: Define the mobile JSON contract from the existing web data shape, add local mock JSON, and build pure domain models/use cases.

Outcomes:

- `manifest.json`, `season.json`, `cars.json`, and `tracks.json` contracts are documented.
- Local mock JSON fixtures exist.
- DTO parsing tests pass.
- Domain models and repository interfaces exist.
- DTO-to-domain mapper tests pass.
- Initial planner use cases exist.

## Sprint 2: Repository, Cache, and Refresh Flow

Goal: Implement data sources, local cache, repository implementations, and refresh/error behavior.

Outcomes:

- Local mock data source works.
- Hosted JSON remote data source is shaped but can remain pointed at mock/local data for MVP.
- Cache storage exists.
- Repository returns cached data when refresh fails.
- Refresh states are represented in presentation-friendly models.

## Sprint 3: Planner UI MVP

Goal: Build the core planner screen.

Outcomes:

- Race list screen exists.
- Week/date selector exists.
- Loading, empty, error, and cached states are handled.
- Planner screen reads from shared state/use cases.
- Android and iOS basic planner flows can be run.

## Sprint 4: Filters, Sorting, and Local Preferences

Goal: Add planner controls and local car/track preferences.

Outcomes:

- Filters exist for owned cars and tracks.
- Favorites exist for cars and tracks.
- Sorting controls exist.
- Preferences persist locally.
- Filter/sort logic is tested.

## Sprint 5: Race Details and Internal Beta Polish

Goal: Complete the internal beta experience.

Outcomes:

- Race detail screen exists.
- Data refresh and offline states are polished.
- Empty/error messaging is usable.
- Basic visual polish is complete.
- Internal beta checklist is complete.
