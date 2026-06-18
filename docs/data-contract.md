# Data Contract

## Strategy

MVP development starts with local mock JSON. The contract should be designed for the mobile planner, while staying derived from the existing web generated data so the web repo can remain the source of truth.

Future hosted data files:

- Mobile data manifest, for example `/data/manifest.json` or `data-manifest.json`
- `season.json`
- `cars.json`
- `tracks.json`

The existing web app already has a PWA `manifest.json` under its static assets. Sprint 1 must document a mobile data manifest separately so the app icon/PWA manifest is not confused with the planner data refresh manifest.

The data layer should parse DTOs from this contract and map them into domain models. Domain and presentation code should not depend on the raw JSON shape.

## Contract Principles

- Include a contract or schema version.
- Include generation metadata.
- Keep stable IDs for cars, tracks, series, weeks, and races.
- Prefer explicit fields over fields that require UI guessing.
- Avoid platform-specific values.
- Keep user preferences out of hosted schedule data.
- Keep owned/favorite cars and tracks as local preference data. Mock schedule/catalog fixtures may include data that exercises those scenarios, but ownership and favorite state should live in separate test inputs or settings storage.

## Mobile Data Manifest

Purpose: tell the app what data files are available and whether a refresh is needed.

The final filename or hosted path should be confirmed in Sprint 1. If it remains `manifest.json`, documentation and hosting should make clear that this is a planner data manifest, not the web app's PWA manifest.

Expected fields:

- `schemaVersion`
- `generatedAt`
- `seasonId`
- `seasonFile`
- `carsFile`
- `tracksFile`
- Optional checksum or revision fields for cache validation

## `season.json`

Purpose: provide season, week, series, and race schedule data.

Expected concepts:

- Season identity
- Race weeks
- Series
- Race sessions
- Car IDs
- Track IDs
- Start times
- Race duration or laps when available
- License/category metadata when available

Open questions for Sprint 1:

- Exact web source fields for race start times
- How to represent recurring race sessions
- Whether week boundaries follow iRacing season week or local calendar week
- Whether sessions need track configuration IDs

## `cars.json`

Purpose: provide the car catalog used by schedule data and local ownership/favorite settings.

Expected fields:

- Stable car ID
- Display name
- Optional class/category
- Optional image or asset reference later

User-owned and favorite state must not be stored in this file. Those are local settings.

## `tracks.json`

Purpose: provide the track catalog used by schedule data and local ownership/favorite settings.

Expected fields:

- Stable track ID
- Display name
- Optional configuration name
- Optional country/location metadata
- Optional image or asset reference later

User-owned and favorite state must not be stored in this file. Those are local settings.

## Local Mock Data

Local mock JSON should:

- Match this contract.
- Include at least one season and one race week.
- Include multiple cars and tracks.
- Include data that can exercise filters and sorting.
- Live somewhere testable from shared code.

Sprint 1 should finalize exact field names after inspecting the current web generated data.

## Sprint 1 Source Audit

Story 1.1 should audit the existing web source of truth before final field names are chosen.

Minimum sources to inspect:

- Web data fixtures/generated JSON for season, cars, and tracks.
- Web race derivation logic for race weeks, session times, recurring sessions, off weeks, car IDs, and track IDs.
- Web season date configuration.
- Web default owned car and track settings.

The audit should record mobile-required fields, web-only fields to ignore, derived fields the mobile app should receive directly, and open questions that block DTO or fixture work.
