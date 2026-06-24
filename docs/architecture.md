# Architecture

## Overview

The app uses Kotlin Multiplatform and Compose Multiplatform with Clean Architecture boundaries.

The core dependency direction is:

```text
platform/app entry points -> presentation -> domain
data -> domain
```

The domain layer is the center. It defines app business concepts and repository interfaces. Data and presentation depend on domain, but domain does not depend on them.

Dependency injection uses Koin. Dagger Hilt should not be used in shared KMP code because Hilt is Android-specific and does not define the shared app dependency graph.

## Layers

### Domain

Purpose:

- Planner business models
- Repository interfaces
- Use cases
- Pure filtering/sorting logic

Allowed dependencies:

- Kotlin standard library
- Kotlin time APIs if needed

Not allowed:

- Compose
- Ktor
- kotlinx.serialization DTO annotations if they make domain follow JSON shape
- Settings/storage APIs
- Android APIs
- iOS APIs
- Platform-specific source sets

### Data

Purpose:

- JSON DTOs
- Serializers
- DTO-to-domain mappers
- Local mock data source
- Future hosted JSON remote data source
- Local cache
- Local settings storage for owned/favorite cars and tracks
- Repository implementations

Allowed dependencies:

- Domain interfaces and models
- Ktor
- kotlinx.serialization
- KMP settings/storage
- Kotlin coroutines if added

### Presentation

Purpose:

- UI state models
- State holders/ViewModels
- Screen state transitions
- Presentation-specific formatting

Allowed dependencies:

- Domain use cases and models
- Compose state APIs where appropriate
- Lifecycle/ViewModel dependencies where appropriate

Presentation should not parse JSON, read cache files directly, or depend on DTOs.

### Platform

Purpose:

- Android app entry point
- iOS app entry point
- Platform-specific dependency wiring
- Platform-specific storage or network configuration when needed

Platform code should be thin. Most behavior belongs in shared domain, data, and presentation code.

## Dependency Injection

Koin is the project DI framework.

Shared/common Koin modules should register:

- Domain use cases
- Repository interfaces to repository implementations
- Data sources
- DTO-to-domain mappers
- Serializers and JSON configuration
- State holders/ViewModels where they are shared

Platform Koin modules should register:

- Android-specific or iOS-specific storage implementations
- Platform-specific HTTP client configuration if needed
- Platform-specific app services

Rules:

- Register dependencies through interfaces where practical.
- Domain models, repository interfaces, and use cases should not import or call Koin APIs directly.
- Platform entry points are responsible for starting Koin and combining common modules with platform modules.
- Android owns shared app dependencies from the `Application` class. Activities consume application-owned dependencies and do not close the app graph during Activity recreation.
- iOS owns shared app dependencies from the Compose controller lifecycle and disposes them with that controller.
- Tests should use Koin test modules or simple fakes to replace repositories, data sources, and state-holder dependencies.

`platform` owns the shared app dependency entry point and combines common Koin modules with thin platform modules such as SQLDelight driver creation.

## Package Direction

Current package organization in shared common code:

```text
shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/
  domain/
    model/        pure business models and domain-safe result/error types
    repository/   domain repository interfaces, one interface per file
    usecase/      domain use cases
  data/
    datasource/   local mock and hosted JSON data sources
    dto/          serializable JSON contract DTOs
    local/        SQLDelight local source of truth
    mapper/       DTO/local-to-domain mapping helpers
    repository/   refresh/cache coordinator and repository implementations
  di/
  presentation/
  platform/
```

Tests should mirror the layer and role package being tested. The Android-host architecture test guards against adding new direct Kotlin files under `domain/` or `data/` root folders, and against grouping multiple domain repository interfaces in one file.

## Testing Strategy

- Use TDD for implementation stories: write or update focused tests before implementation when the behavior can be meaningfully tested.
- Domain: use fake repositories and pure unit tests.
- Data DTOs: parse local mock JSON fixtures.
- Mappers: verify conversion from DTOs to domain models.
- Repositories: verify refresh, cache fallback, and error behavior.
- Presentation: verify loading, loaded, empty, cached, and error states.
- DI: smoke-test baseline Koin modules when wiring becomes non-trivial.

## Boundary Checks

Before completing a story, check:

- Did any domain file import Compose, Ktor, serialization DTOs, storage, Android, or iOS APIs?
- Did UI code consume raw DTOs?
- Did data code leak cache/network details into domain?
- Did domain code import Koin or platform DI APIs?
- Did new behavior get covered by focused tests?
