# iRacing Week Planner Mobile Agent Guide

This repository is the dedicated mobile app for iRacing Week Planner. Treat this file as the first handoff document for AI agents and developers.

## Project Direction

- Build a Kotlin Multiplatform + Compose Multiplatform app for Android and iOS.
- Use Clean Architecture with clear `domain`, `data`, `presentation`, and platform-specific boundaries.
- Use Koin for dependency injection across shared and platform code.
- Keep the existing web repo and scraper as the source of truth for schedule generation.
- The mobile app must not scrape iRacing, store iRacing credentials, or depend on Firebase sync in v1.
- MVP data should start from local mock JSON fixtures. The data layer should be shaped so hosted JSON can replace the mock source later.
- First release target is an internal beta.

## Architecture Rules

- `domain` is pure Kotlin: models, repository interfaces, and use cases only.
- `domain` must not depend on Compose, Ktor, storage, serialization DTOs, Android, iOS, or platform APIs.
- `data` owns DTOs, serializers, mappers, local mock/remote data sources, cache storage, and repository implementations.
- `presentation` owns Compose UI state, state holders/ViewModels, and UI models.
- Platform-specific wiring belongs in Android/iOS entry points or platform source sets.
- UI should depend on presentation state and domain use cases, not raw network/cache DTOs.
- Koin modules should wire dependencies through interfaces where practical.
- Domain models, repository interfaces, and use cases should not depend on Koin APIs directly.
- Do not use Dagger Hilt in shared KMP code. Hilt is Android-specific context, not the shared DI architecture.

## Data Strategy

- MVP uses local mock JSON files.
- Future hosted JSON files are:
  - `manifest.json`
  - `season.json`
  - `cars.json`
  - `tracks.json`
- Cache-after-fetch behavior is required before beta: after the first successful refresh, the app should remain usable offline.
- Owned and favorite settings are local-only in v1 and apply to cars and tracks only.

## Work Style

- Work story-by-story. Do not implement multiple sprint stories unless explicitly asked.
- Before implementation, check `docs/roadmap.md` and the active sprint story file.
- Keep changes small, reviewable, and tied to acceptance criteria.
- Update docs when behavior, architecture, commands, or constraints change.
- Use TDD for implementation stories: write or update focused tests first for domain logic, DTO parsing, mappers, repository/cache behavior, and state holders.
- Documentation-only changes and pure wiring that cannot be meaningfully tested may skip the red test step, but should still include verification.
- Do not add Firebase, account login, cloud sync, or mobile scraping unless the product scope changes.

## Verification Expectations

Run the narrowest useful verification before claiming work is complete. Common commands:

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :androidApp:assembleDebug
./gradlew :shared:iosSimulatorArm64Test
```

If a command cannot run because of local tooling, record the exact command, failure, and likely blocker in the relevant doc or final handoff.

## Known Context

- The mobile repo root is the Gradle root. Do not assume a nested `mobile/` directory in this checkout.
- Current modules are expected to be `androidApp`, `iosApp`, and `shared`.
- Use common/popular platform targets unless a product decision changes them.
