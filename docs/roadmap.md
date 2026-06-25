# Roadmap

## Cross-Sprint Design Runway

Adaptive and responsive design should be considered in every UI story, even when the story remains phone-first. Sprint 3 and Sprint 4 should avoid choices that force a later large-scale refactor: screens should stay stateless where practical, interactions should flow through callbacks, display models should keep stable identifiers needed for future navigation, and app-level chrome should not be mixed into business state.

Navigation 3 is not part of the Sprint 3 MVP. It should be introduced with the first real navigation feature, currently planned as the Schedule list to Race Detail flow in Sprint 5. Exact tablet, iPad, and breakpoint behavior remains a Sprint 5 design decision.

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
- Invalid required source data is represented as an error state, not silently coerced or dropped.

## Sprint 3: Planner UI MVP

Goal: Build the core planner screen.

Outcomes:

- Race list screen exists.
- Week/date selector exists.
- Loading, empty, error, and cached states are handled.
- Invalid required data is shown as a UI error instead of being hidden silently.
- Planner screen reads from shared state/use cases.
- Android and iOS basic planner flows can be run.
- Schedule components remain compatible with future adaptive Navigation 3 list-detail work.

## Sprint 4: Filters, Sorting, and Local Preferences

Goal: Add planner controls and local car/track preferences.

Outcomes:

- Filters exist for owned cars and tracks.
- Favorites exist for cars and tracks.
- Sorting controls exist.
- Preferences persist locally.
- Filter/sort logic is tested.
- Filter, sorting, and preference controls remain reusable in compact and future expanded layouts.

## Sprint 5: Race Details and Internal Beta Polish

Goal: Complete the internal beta experience.

Outcomes:

- Race detail screen exists.
- Navigation 3 compatibility is verified on Android and iOS before route/adaptive APIs become required.
- Schedule list to Race Detail uses shared route state and is ready for adaptive list-detail rendering.
- Data refresh and offline states are polished.
- Empty/error messaging is usable.
- Basic visual polish is complete.
- Internal beta checklist is complete.
