# Data Contract

## Strategy

MVP development starts with local mock JSON. The contract should be designed for the mobile planner, while staying derived from the existing web generated data so the web repo can remain the source of truth.

Future hosted data files:

- `manifest.json`
- `season.json`
- `cars.json`
- `tracks.json`

The data layer should parse DTOs from this contract and map them into domain models. Domain and presentation code should not depend on the raw JSON shape.

## Contract Principles

- Include a contract or schema version.
- Include generation metadata.
- Keep stable IDs for cars, tracks, series, weeks, and races.
- Prefer explicit fields over fields that require UI guessing.
- Avoid platform-specific values.
- Keep user preferences out of hosted schedule data.

## `manifest.json`

Purpose: tell the app what data files are available and whether a refresh is needed.

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
