# Sprint 0 User Stories: Project Setup and Technical Spike

## Sprint Goal

Prove that a Kotlin Multiplatform + Compose Multiplatform mobile app can be built for Android and iOS in this repository, with Clean Architecture boundaries in place and a documented local development workflow.

This mobile checkout is already the dedicated mobile repository. The Gradle root is the repository root; do not create or assume a nested `mobile/` directory here.

## Story 0.1: Confirm Mobile Workspace

**As a mobile developer, I want a dedicated mobile workspace so that the KMP app can evolve independently from the existing web app.**

### Acceptance Criteria

- This repository is treated as the dedicated mobile workspace.
- The project has clear Gradle module naming for the iRacing planner app.
- Mobile project files do not require changes to the existing web repo.
- Base package naming is documented.

### Implementation Tasks

- Confirm `settings.gradle.kts` and module structure.
- Confirm expected modules: `androidApp`, `iosApp`, and `shared`.
- Document that the old web repo remains the scraper/source-of-truth repo.
- Choose or confirm base package name, recommended: `com.iracingweekplanner.mobile`.

## Story 0.2: Add KMP Shared Module

**As a developer, I want a shared Kotlin module so that domain, data, and presentation logic can be reused across Android and iOS.**

### Acceptance Criteria

- A shared KMP module exists.
- The shared module has common, Android, and iOS source sets.
- The shared module compiles for Android and iOS targets.
- Shared code exposes a simple value or function consumed by the app layer.

### Implementation Tasks

- Confirm or create the `shared` module.
- Configure Kotlin Multiplatform targets:
  - Android
  - iOS simulator/device targets
- Add Kotlin test dependency for shared tests.
- Add a simple shared smoke-test class or function.

## Story 0.3: Establish Clean Architecture Boundaries

**As a developer, I want the initial project structure to reflect Clean Architecture so that future sprint work does not mix UI, business rules, and data access.**

### Acceptance Criteria

- Shared code has package folders for:
  - `domain`
  - `data`
  - `presentation`
  - `platform`
- Domain code has no dependency on Compose, networking, storage, or platform APIs.
- Data code depends on domain abstractions, not the other way around.
- Presentation code depends on domain use cases/state, not raw DTOs.

### Implementation Tasks

- Add package structure in `shared/src/commonMain/kotlin`.
- Add initial domain model placeholder, such as `PlannerAppInfo`.
- Add an initial domain use case placeholder, such as `GetAppInfoUseCase`.
- Add a small presentation state holder that consumes the use case.
- Add comments only where needed to clarify layer boundaries.

## Story 0.4: Add Baseline Dependencies

**As a developer, I want the baseline multiplatform dependencies installed so that later sprints can implement network fetch, JSON parsing, dependency injection, settings storage, and tests.**

### Acceptance Criteria

- Dependency versions are centralized.
- The shared module has dependencies for:
  - Ktor client
  - kotlinx.serialization
  - Koin
  - KMP-compatible settings/storage
  - Kotlin test
- Compose Multiplatform dependencies are available to the app UI layer.
- A minimal Koin smoke setup exists or is explicitly documented as the next wiring task.
- The project syncs without unused platform-specific configuration errors.

### Implementation Tasks

- Add or update Gradle version catalog entries.
- Add Ktor client dependencies.
- Add kotlinx.serialization plugin and JSON dependency.
- Add Koin dependencies and baseline modules.
- Add Multiplatform Settings or equivalent storage dependency.
- Add Compose Multiplatform plugin/dependencies.
- Add a minimal Koin module smoke test if dependencies are wired in this story.

## Story 0.5: Create Android Runnable Target

**As a mobile developer, I want an Android app target so that I can run the Compose placeholder app on an emulator or device.**

### Acceptance Criteria

- An Android app module exists.
- Android app launches a placeholder Compose screen.
- Placeholder screen reads at least one value from shared code.
- Android debug build succeeds.
- Minimum SDK and target SDK are explicitly configured.

### Implementation Tasks

- Confirm or create Android app module.
- Add Android manifest.
- Add `MainActivity`.
- Wire Compose UI.
- Consume shared module from Android.
- Add app name resource.

## Story 0.6: Create iOS Runnable Target

**As a mobile developer, I want an iOS app target so that I can run the Compose placeholder app on an iOS simulator or device.**

### Acceptance Criteria

- An iOS app project or target exists.
- iOS app launches a placeholder Compose screen.
- Placeholder screen reads at least one value from shared code.
- The shared KMP framework is integrated into the iOS app.
- iOS simulator build instructions are documented.

### Implementation Tasks

- Confirm or create iOS app structure under `iosApp`.
- Add Swift entry point or Compose Multiplatform iOS host.
- Configure shared framework integration.
- Add basic app metadata.
- Verify the iOS project can be opened from Xcode.

## Story 0.7: Add Shared Smoke Tests

**As a developer, I want shared smoke tests so that the KMP test setup is proven before domain logic is added in Sprint 1.**

### Acceptance Criteria

- At least one shared test exists.
- The test exercises shared domain or presentation setup.
- Shared tests can be run from Gradle.
- Test command is documented.

### Implementation Tasks

- Add a simple use case test.
- Add a presentation state test if the initial state holder exists.
- Confirm the Gradle test task name.
- Record the command in development docs.

## Story 0.8: Document Local Mobile Setup

**As a mobile developer, I want setup documentation so that I can run Android, iOS, and tests consistently from a clean checkout.**

### Acceptance Criteria

- Local setup documentation exists.
- Documentation includes required tools:
  - JDK
  - Android Studio
  - Xcode
  - Kotlin/Gradle usage
- Documentation includes commands for:
  - Gradle sync/build
  - Android debug build
  - Shared tests
  - iOS simulator build/opening the project
- Documentation states that the existing web app remains separate.

### Implementation Tasks

- Update `README.md` or `docs/development.md`.
- Add project structure overview.
- Add setup commands.
- Add troubleshooting notes for Gradle sync and iOS framework generation.
- Add Sprint 0 exit checklist.

## Story 0.9: Verify Sprint 0 Exit Criteria

**As the project owner, I want a final Sprint 0 verification checklist so that I know the technical spike is complete and Sprint 1 can start safely.**

### Acceptance Criteria

- Android app runs or has a documented blocker.
- iOS app runs or has a documented blocker.
- Shared tests pass or have a documented blocker.
- Clean Architecture package boundaries exist.
- Local setup commands are documented.
- Known setup risks are recorded before Sprint 1 begins.

### Implementation Tasks

- Run Android build command.
- Run shared test command.
- Run or document iOS simulator build command.
- Record results in `docs/development.md`.
- Create follow-up backlog items for any blockers.

## Sprint 0 Definition of Done

- Android and iOS targets are scaffolded.
- Shared KMP code compiles.
- Shared smoke tests exist.
- Clean Architecture package boundaries are visible.
- Local setup documentation exists.
- Any unverified platform run step is documented as a blocker with the exact command attempted.
