# Sprint 1 User Stories: Data Contract and Domain Foundation

## Sprint Goal

Define the mobile data contract, add local mock JSON, and create the domain foundation needed for planner data.

The mobile contract should be designed for the app, but derived from the existing web repo/generated data so the scraper remains the source of truth.

## Story 1.1: Inspect Existing Web Data Shape

**As a developer, I want to inspect the current web generated data so that the mobile contract is grounded in real schedule output.**

### Acceptance Criteria

- The relevant generated data shape in the web repo is identified.
- Required fields for mobile planner MVP are listed.
- Web-only fields and mobile-only derived fields are documented.
- Open questions are recorded in `docs/data-contract.md`.

## Story 1.2: Define Mobile JSON Contract

**As a developer, I want a documented JSON contract so that mobile parsing, mock data, and future hosted data agree on shape.**

### Acceptance Criteria

- `manifest.json` contract is documented.
- `season.json` contract is documented.
- `cars.json` contract is documented.
- `tracks.json` contract is documented.
- Versioning or generated-at metadata is included.
- Required and optional fields are clear.

## Story 1.3: Add Local Mock JSON Fixtures

**As a developer, I want local mock JSON fixtures so that the MVP can be built before hosted JSON is finalized.**

### Acceptance Criteria

- Mock fixtures exist in a shared-testable location.
- Fixtures include at least one race week.
- Fixtures include multiple cars and tracks.
- Fixtures cover owned/favorite filter test scenarios.
- Fixtures match `docs/data-contract.md`.

## Story 1.4: Add DTOs and Serialization Tests

**As a developer, I want DTOs and parser tests so that contract changes fail loudly.**

### Acceptance Criteria

- DTOs exist for manifest, season, cars, and tracks.
- DTOs are isolated in the data layer.
- kotlinx.serialization parses every mock fixture.
- Tests cover required fields and representative optional fields.

## Story 1.5: Add Domain Models

**As a developer, I want pure domain models so that planner logic does not depend on JSON or platform details.**

### Acceptance Criteria

- Domain models exist for races, series, cars, tracks, weeks, and planner filters as needed.
- Domain models are pure Kotlin.
- Domain models do not import DTOs, Compose, storage, Ktor, Android, or iOS APIs.
- Domain naming reflects planner concepts rather than raw JSON structure.

## Story 1.6: Add DTO-to-Domain Mappers

**As a developer, I want mapper tests so that app behavior is protected from raw data shape changes.**

### Acceptance Criteria

- Mappers convert DTOs into domain models.
- Mapper tests cover normal data.
- Mapper tests cover missing optional fields where allowed.
- Invalid required data has a clear failure strategy.

## Story 1.7: Add Initial Repository Interfaces and Use Cases

**As a developer, I want domain repository interfaces and use cases so that later data and UI work can depend on stable contracts.**

### Acceptance Criteria

- Domain repository interfaces exist for planner data.
- Initial use cases exist for loading race weeks, cars, tracks, and planner races.
- Use cases are tested with fake repositories.
- Use cases do not know whether data comes from mock JSON, hosted JSON, or cache.

## Sprint 1 Definition of Done

- Data contract is documented.
- Mock JSON fixtures exist.
- DTO serialization tests pass.
- Domain models are pure Kotlin.
- Mapper tests pass.
- Initial repository interfaces and use cases exist.
- Sprint 2 can implement local/remote/cache repositories against stable interfaces.
