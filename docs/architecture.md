# Architecture

## Overview

The app uses Kotlin Multiplatform and Compose Multiplatform with Clean Architecture boundaries.

The core dependency direction is:

```text
platform/app entry points -> presentation -> domain
data -> domain
```

The domain layer is the center. It defines app business concepts and repository interfaces. Data and presentation depend on domain, but domain does not depend on them.

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

## Package Direction

Target package organization in shared code:

```text
shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/
  domain/
  data/
  presentation/
  platform/
```

Tests should mirror the layer being tested.

## Testing Strategy

- Domain: use fake repositories and pure unit tests.
- Data DTOs: parse local mock JSON fixtures.
- Mappers: verify conversion from DTOs to domain models.
- Repositories: verify refresh, cache fallback, and error behavior.
- Presentation: verify loading, loaded, empty, cached, and error states.

## Boundary Checks

Before completing a story, check:

- Did any domain file import Compose, Ktor, serialization DTOs, storage, Android, or iOS APIs?
- Did UI code consume raw DTOs?
- Did data code leak cache/network details into domain?
- Did new behavior get covered by focused tests?
