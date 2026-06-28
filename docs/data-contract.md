# Data Contract

For the current generated Supabase/static JSON contract used by frontend
integration, see [Hosted JSON API](hosted-json-api.md). This document keeps the
broader contract principles and older planning context.

## Strategy

MVP development starts with local mock JSON. The contract should be designed for the mobile planner, while staying derived from the existing web generated data so the web repo can remain the source of truth.

Future hosted data files:

- Mobile planner data manifest: `/data/mobile/v1/manifest.json`
- `season.json`
- `cars.json`
- `tracks.json`

The existing web app already has a PWA `manifest.json` under its static assets. Sprint 1 must document a mobile data manifest separately so the app icon/PWA manifest is not confused with the planner data refresh manifest.

The mobile app should treat `/data/mobile/v1/manifest.json` as the entry point. The manifest file points to the other JSON files with paths relative to the manifest URL, such as `season.json`, `cars.json`, and `tracks.json`. Local mock fixtures added in Story 1.3 should use the same filenames and JSON shapes.

The data layer should parse DTOs from this contract and map them into domain models. Domain and presentation code should not depend on the raw JSON shape.

## Contract Principles

- Every hosted file includes `schemaVersion` and `generatedAt`.
- Keep stable IDs for cars, tracks, series, weeks, and races.
- Prefer explicit fields over fields that require UI guessing.
- Avoid platform-specific values.
- Keep user preferences out of hosted schedule data.
- Keep owned/favorite cars and tracks as local preference data. Mock schedule/catalog fixtures may include data that exercises those scenarios, but ownership and favorite state should live in separate test inputs or settings storage.
- Use ISO-8601 UTC strings for timestamps, for example `2026-06-16T00:00:00Z`.
- Use minutes for time offsets and durations when the source data is not a timestamp.

## Common Metadata

Each JSON file starts with these fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `schemaVersion` | Integer | Yes | Start at `1`. Increment only when parsing compatibility changes. |
| `generatedAt` | String | Yes | ISO-8601 UTC timestamp for the export. |

Files that describe a season also include:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `seasonId` | String | Yes | Stable season identifier derived from web `seasonid`. Use a string in JSON so the app is not tied to a numeric source type. |

## Mobile Data Manifest

Purpose: tell the app what data files are available and whether a refresh is needed.

Hosted path: `/data/mobile/v1/manifest.json`.

This is the mobile planner data manifest. It is not the web/PWA manifest, and it should not be served from the web app's root `/manifest.json` path.

Required fields:

| Field | Type | Notes |
| --- | --- | --- |
| `schemaVersion` | Integer | Manifest schema version. |
| `generatedAt` | String | Export timestamp in UTC. |
| `seasonId` | String | Season represented by the referenced data files. |
| `seasonFile` | String | Relative URL for `season.json`. |
| `carsFile` | String | Relative URL for `cars.json`. |
| `tracksFile` | String | Relative URL for `tracks.json`. |

Optional fields:

| Field | Type | Notes |
| --- | --- | --- |
| `revision` | String | Opaque scraper/export revision, commit SHA, or content revision. |
| `checksums` | Object | Map from referenced filename to checksum string for cache validation. |

Example:

```json
{
  "schemaVersion": 1,
  "generatedAt": "2026-06-16T00:00:00Z",
  "seasonId": "2026-s2",
  "seasonFile": "season.json",
  "carsFile": "cars.json",
  "tracksFile": "tracks.json",
  "revision": "2026-s2-001",
  "checksums": {
    "season.json": "sha256:...",
    "cars.json": "sha256:...",
    "tracks.json": "sha256:..."
  }
}
```

## `season.json`

Purpose: provide season, week, series, and race schedule data.

Required top-level fields:

| Field | Type | Notes |
| --- | --- | --- |
| `schemaVersion` | Integer | Season file schema version. |
| `generatedAt` | String | Export timestamp in UTC. |
| `seasonId` | String | Stable season identifier derived from web `seasonid`. |
| `seasonName` | String | Display label, for example `2026 Season 2`. |
| `seasonStart` | String | Season start timestamp in UTC. |
| `seasonEnd` | String | Season end timestamp in UTC. |
| `weekSeasonStart` | String | UTC timestamp used for current-week calculation. |
| `weeks` | Array | Race-week metadata. |
| `series` | Array | Series metadata. |
| `races` | Array | Planner-ready race rows. |

`weeks[]` entries:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `weekNumber` | Integer | Yes | iRacing race week number. |
| `startsAt` | String | Yes | Week start timestamp in UTC. |
| `endsAt` | String | Yes | Week end timestamp in UTC. |

`series[]` entries:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `seriesId` | String | Yes | Stable series identifier derived from web `seriesid`. |
| `name` | String | Yes | Series display name from web `seriesname`. |
| `category` | String | Yes | Normalized category label such as `Road`, `Oval`, `Sports Car`, or `Formula Car`. |
| `license` | Object | Yes | Display and filter metadata for license requirements. |
| `isOfficial` | Boolean | Yes | Whether the series is official. |
| `isFixedSetup` | Boolean | Yes | Whether the series uses fixed setup. |

`license` object:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `className` | String | Yes | Display class such as `Rookie`, `D`, `C`, `B`, `A`, or `Pro`. |
| `level` | Integer | No | Source numeric license level when available. |

`races[]` entries:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `raceId` | String | Yes | Stable ID. For v1 use a composite of season ID, series ID, week number, track package ID, and race start timestamp. |
| `seriesId` | String | Yes | Links to `series[].seriesId`. |
| `weekNumber` | Integer | Yes | Links to `weeks[].weekNumber`. |
| `startsAt` | String | Yes | Race-week start timestamp in UTC after off-week adjustments. |
| `endsAt` | String | Yes | Race-week end timestamp in UTC. |
| `trackPackageId` | String | Yes | Track ownership/filter key derived from web `pkgid`. |
| `trackName` | String | Yes | Display track/package name. |
| `carSkus` | Array of strings | Yes | Car ownership/filter keys derived from web `sku`. |
| `carClasses` | Array of strings | Yes | Display car class labels. Empty array is allowed when the source has no class mapping. |
| `sessions` | Array | Yes | Structured session timing rules. |
| `trackConfigName` | String | No | Configuration/layout display name when known separately from `trackName`. |
| `raceLength` | Object | No | Race length as laps and/or minutes. |
| `precipChance` | Number | No | Rain chance from source data when available. |

`raceLength` object:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `laps` | Integer | No | Include when the source describes race length by laps. |
| `minutes` | Integer | No | Include when the source describes race length by time. |

`sessions[]` entries use one of these shapes:

```json
{
  "type": "recurring",
  "firstSessionOffsetMinutes": 60,
  "repeatEveryMinutes": 120
}
```

```json
{
  "type": "setTimes",
  "offsetMinutes": [120, 480, 840]
}
```

Session fields:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `type` | String | Yes | Either `recurring` or `setTimes`. |
| `firstSessionOffsetMinutes` | Integer | For `recurring` | Minutes after `races[].startsAt` for the first session. |
| `repeatEveryMinutes` | Integer | For `recurring` | Repeat interval from web `repeat_minutes`. |
| `offsetMinutes` | Array of integers | For `setTimes` | Session offsets from `races[].startsAt`. |

The season file publishes planner-ready race rows. Mobile shared code should not duplicate web off-week insertion or race-week date derivation for v1.

## `cars.json`

Purpose: provide the car catalog used by schedule data and local ownership/favorite settings.

Required top-level fields:

| Field | Type | Notes |
| --- | --- | --- |
| `schemaVersion` | Integer | Cars file schema version. |
| `generatedAt` | String | Export timestamp in UTC. |
| `cars` | Array | Car catalog entries. |

`cars[]` entries:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `sku` | String | Yes | Stable car ownership/filter key from web `sku`. |
| `displayName` | String | Yes | User-facing car name. Prefer web `name`; fall back to `skuname` only if needed. |
| `sourceCarId` | Integer | No | iRacing `car_id` for source reconciliation. |
| `sourceSkuName` | String | No | Raw web `skuname` if it differs from display name. |
| `categories` | Array of strings | No | Source category labels when useful for grouping. |
| `carClasses` | Array of strings | No | Class labels when available. |
| `freeWithSubscription` | Boolean | No | Include if local default ownership seeding stays aligned with web defaults. |
| `imageUrl` | String | No | Future asset URL. Omit for MVP fixtures unless a real asset exists. |

User-owned and favorite state must not be stored in this file. Those are local settings.

## `tracks.json`

Purpose: provide the track catalog used by schedule data and local ownership/favorite settings.

Required top-level fields:

| Field | Type | Notes |
| --- | --- | --- |
| `schemaVersion` | Integer | Tracks file schema version. |
| `generatedAt` | String | Export timestamp in UTC. |
| `tracks` | Array | Track catalog entries. |

`tracks[]` entries:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `packageId` | String | Yes | Stable track ownership/filter key derived from web `pkgid`. |
| `displayName` | String | Yes | User-facing track/package name. |
| `sourceTrackIds` | Array of integers | Yes | Concrete iRacing track/config IDs from web `ids`. Empty array is allowed only when the source cannot provide IDs. |
| `type` | String | No | Primary type label such as `road`, `oval`, `dirtOval`, or `dirtRoad`. |
| `supportedTypes` | Array of strings | No | Type flags from source data when a package supports multiple disciplines. |
| `isDefaultContent` | Boolean | No | Include if local default ownership seeding stays aligned with web defaults. |
| `mapUrl` | String | No | Track map URL from source data when available. |
| `imageUrl` | String | No | Future asset URL. Omit for MVP fixtures unless a real asset exists. |

User-owned and favorite state must not be stored in this file. Those are local settings.

## User Preference Data Exclusion

Hosted `manifest.json`, `season.json`, `cars.json`, and `tracks.json` must not contain:

- `ownedCars`
- `ownedTracks`
- `favoriteCars`
- `favoriteTracks`
- account identifiers
- iRacing credentials
- Firebase sync state

Owned and favorite settings are local-only v1 data. Domain and data-layer tests may create separate preference inputs to exercise filter behavior, but those preferences are not part of hosted schedule or catalog JSON.

## Local Mock Data

Local mock JSON should:

- Match this contract.
- Include at least one season and one race week.
- Include multiple cars and tracks.
- Include data that can exercise filters and sorting.
- Live somewhere testable from shared code.

Story 1.3 fixtures use the exact field names defined above. The hosted-shape mock files live under
`shared/src/commonMain/composeResources/files/mock-data/`:

- `manifest.json`
- `season.json`
- `cars.json`
- `tracks.json`

Separate local preference test data lives under
`shared/src/commonTest/resources/mock-data/local-preferences.json` so owned/favorite scenarios can be tested
without adding user preference fields to hosted schedule or catalog JSON.

## Sprint 1 Source Audit

Story 1.1 audited the existing web source of truth before Story 1.2 finalized the mobile JSON field names.

Source repo audited: `/Users/gaojiahao/Documents/iracing/iRacing-week-planner`.

The web repo keeps real scraped files out of git:

- `/src/data/season.json`
- `/src/data/cars.json`
- `/src/data/tracks.json`
- `/src/data/car-class.json`
- `/src/data/contributors.json`

Those files are written by `build/scrape.js` and ignored by `.gitignore`. The committed source-data baseline for this audit is therefore the scraper code plus the committed mock JSON under `src/data/__mocks__/`.

### Source-Data Audit Table

| Area | Web source | Current shape or rule | Mobile contract impact |
| --- | --- | --- | --- |
| Generated season data | `build/scrape.js`, `build/api/getSeason.js`, `src/data/__mocks__/season.json` | Scraper writes `src/data/season.json` from `/data/series/seasons`, car classes, licenses, cars, and tracks. Mock contains 2 series and 23 race rows. | `season.json` should use generated schedule data as the source, but expose a mobile-friendly shape instead of requiring app-side reconstruction from the raw web series shape. |
| Generated car catalog | `build/api/getCars.js`, `src/data/__mocks__/cars.json`, `src/data/index.js` | Car records include `id` from iRacing `car_id`, `sku`, `name`, `skuname`, `categories`, `freeWithSubscription`, `price`, and `discountGroupNames`. Web de-duplicates cars by `sku`. | Mobile needs stable car IDs, display names, categories/classes where useful, and the `freeWithSubscription` flag only if we want to seed default local ownership. |
| Generated track catalog | `build/api/getTracks.js`, `src/data/__mocks__/tracks.json` | Track records are grouped by `package_id` into `pkgid`. Each record also keeps all concrete iRacing `track_id` values in `ids`, plus name, default/free flag, type booleans, price, and optional map URL. | Mobile should use `pkgid` as the ownership/filter ID and keep concrete track/config IDs separately if later needed for display or links. |
| Race derivation | `src/lib/races.js` | Web converts series/tracks into `SeriesRace` rows with series, track, IDs, week, derived `startTime`, `endTime`, `weekLength`, license, type, fixed/official flags, car classes, car IDs, race length, precipitation, and session timing. | Mobile should receive these planner-ready race rows, or enough documented fields to create exactly the same rows in shared code. Pre-deriving server-side is lower risk for v1. |
| Off-week handling | `src/data/offWeeks.js`, `src/lib/races.js` | Off weeks are keyed by `seasonid`. Web inserts decimal off-week markers into the week list and can apply `weekStartOffset` or `weekEndOffset` before deriving race-week dates. | Mobile contract should either receive already-derived race-week start/end dates or include explicit off-week metadata. Avoid hidden off-week logic in UI code. |
| Season date config | `src/config.js`, `src/reducers/app.js`, `src/lib/races.js` | Web has global `seasonStart`, `seasonEnd`, and `weekSeasonStart`; app week is `ceil((date + 1 second - weekSeasonStart).asWeeks())`. Race rows also derive dates from each series `start` and `end`. | Mobile needs season-level `seasonStart`, `seasonEnd`, `weekSeasonStart`, and per-race `startsAt`/`endsAt` or equivalent, all in UTC. |
| Session-time rules | `src/lib/races.js`, `src/components/columns/RaceTimes.js` | Repeating sessions use `repeat_minutes` and `first_session_time`. Set-time sessions use explicit `session_times`, converted into offsets from the derived race-week `startTime`. | Mobile should model either `recurrence` or `setTimes` explicitly. Avoid requiring UI code to diff raw iRacing timestamps against derived starts. |
| Default owned content | `src/reducers/settings.js` | Default owned cars are `cars.filter(freeWithSubscription).map(sku)`. Default owned tracks are `tracks.filter(default).map(pkgid)`. Mock catalog has 20 free cars and 22 default tracks. | Owned/favorite settings remain local-only, but mock/test preference inputs should use car `sku` and track `pkgid` to match web behavior. |
| Filtering | `src/lib/filterRaces.js` | Filters compare `race.trackId` against owned/favorite track package IDs and intersect `race.carIds` with owned/favorite car SKU lists. | Mobile filter domain should use the same ID choices: car ownership by SKU, track ownership by package ID. |
| Purchase optimization | `src/lib/purchaseOptimization.js` | Counts unowned track `pkgid` appearances across favorite series. | Not MVP schedule data, but reinforces `pkgid` as the track ownership key. |
| PWA manifest and web-only app data | `dist/static/manifest.json`, Firebase config in `src/config.js`, changelog/contributors data | These support the web app shell, Firebase sync, changelog, and contributor display. | Exclude from mobile schedule/catalog contract. The mobile data manifest must be documented separately from the PWA manifest. |

### Required Mobile Planner MVP Fields

The MVP planner needs these fields after the source audit. The Story 1.2 contract above maps these concepts into exact JSON names.

Season and week metadata:

- Contract/schema version.
- `generatedAt` timestamp.
- Season ID or season label.
- Season start and end timestamps in UTC.
- Week-calculation start timestamp in UTC.
- Race weeks with stable week number, start timestamp, and end timestamp.

Series and race data:

- Stable series ID from web `seriesid`.
- Series display name from web `seriesname`.
- Category/type derived from `catid` (`Oval`, `Road`, `Dirt`, `RX`, `Sports Car`, `Formula Car` in current web code).
- License class/level data needed for display and filtering.
- Official flag.
- Fixed setup flag.
- Race row ID or stable composite key. A practical v1 key is season ID, series ID, race week, track package ID, and start timestamp.
- Race week number from web `raceweek`.
- Race start and end timestamps in UTC.
- Track package ID for ownership/filtering.
- Track display name and optional config name.
- Car SKU IDs for ownership/filtering.
- Car class display names.
- Race length as laps and/or minutes.
- Session timing as either repeating recurrence or explicit set times.
- Optional precipitation chance if the mobile UI keeps the rain indicator.

Car catalog:

- Stable car SKU for schedule references and owned/favorite settings.
- Source iRacing car ID if needed for future reconciliation.
- Display name.
- Categories/classes where useful for filtering or grouping.
- Free-with-subscription flag if local default ownership seeding stays aligned with web.

Track catalog:

- Stable package ID (`pkgid`) for schedule references and owned/favorite settings.
- Source iRacing track/config IDs (`ids`) if needed for future reconciliation.
- Display name.
- Primary type and supported type flags when useful for filtering.
- Free/default flag if local default ownership seeding stays aligned with web.
- Optional map/image asset reference later.

### Web-Only Fields to Exclude

- Firebase config and Firebase sync state.
- Web login/auth settings.
- PWA/static asset manifest data from `dist/static/manifest.json`.
- Contributor and changelog data.
- Web column preferences and local table sort defaults.
- Purchase prices and discount group names unless a future mobile purchase-planning story needs them.
- Web UI component-specific labels or modal state.
- Raw `moment`-specific duration objects; JSON should use ISO timestamps, minutes, or structured recurrence fields.

### Mobile-Required Fields Not Directly Present in Web JSON

These should be derived before or during mobile contract generation:

- Planner data manifest metadata: schema version, generated timestamp, file paths, and optional checksums/revisions.
- Stable mobile race IDs or composite race keys.
- Explicit race-week `startsAt` and `endsAt` timestamps.
- Explicit session timing DTOs instead of raw `race_time_descriptors`.
- Normalized category labels if the mobile app should not duplicate the web `catid` map.
- Normalized license display/filter fields if the mobile app should not duplicate `levelToClass` logic.
- Optional separate `trackName` and `trackConfigName`; the web currently stores a combined `name` and sometimes `config`.

### ID Choices

- Car ownership and favorite filters should use car SKU values. Web `defaultSettings.ownedCars`, `filterRaces`, and `SeriesRace.carIds` all use `sku`.
- Keep source iRacing `car_id` as a secondary catalog field only if useful for future scraper reconciliation.
- Track ownership and favorite filters should use package IDs. Web `defaultSettings.ownedTracks`, `filterRaces`, `purchaseOptimization`, and `SeriesRace.trackId` all use `pkgid`.
- Keep concrete iRacing track/config IDs (`ids`) as secondary catalog fields. They are not the right owned-content key because one purchasable track package can contain multiple configurations.
- Series IDs should use web `seriesid`.
- Season IDs should use web `seasonid`.

### Race-Week and Session-Time Derivation Rules

Current web derivation in `src/lib/races.js`:

1. For each series, read off-week data by `seasonid` from `src/data/offWeeks.js`.
2. Start with `moment(series.start).utc().startOf('day')`.
3. Apply `weekStartOffset` when present.
4. Compute series end as `moment(series.end).utc().startOf('isoWeek').add({ days: 1 })`.
5. Apply `weekEndOffset` when present.
6. Compute race-week length as `(seriesEnd - seriesStart) / (number of scheduled tracks + number of off-week entries)`.
7. Build `allRaceWeeks` from each track `raceweek` plus each off-week marker minus 1, then sort it.
8. For each scheduled track, find its real week index in `allRaceWeeks`.
9. Derive race `startTime` as `seriesStart + raceWeekLength * realRaceWeek`, rounded to start of UTC day.
10. Derive `endTime` as `startTime + weekLength`.
11. Use track-specific `carsForWeek` when present; otherwise use the series car SKU list.
12. For repeating sessions, use `repeat_minutes` and `first_session_time`.
13. For set-time sessions, convert each source `session_times` timestamp into an offset from derived `startTime`.

Current web week selection in `src/reducers/app.js`:

1. Clamp today's UTC date to `seasonStart <= date < seasonEnd`.
2. Store `daysSinceSeasonStart` from `seasonStart`.
3. Compute displayed week as `ceil((date + 1 second - weekSeasonStart).asWeeks())`.

Mobile contract guidance:

- Prefer publishing explicit race start/end timestamps and structured session rules so mobile shared code does not have to duplicate off-week and date math.
- If mobile must derive this locally, the contract must include off-week entries, week offsets, raw series start/end, raw track race weeks, and the exact week-start rule.

### Remaining Follow-Up Questions

These are not blockers for Story 1.2, but should be revisited when hosted data generation or later UI stories are implemented:

- Should `precipChance` be populated for the MVP UI or left absent until a rain indicator story needs it?
- Should optional catalog default flags be generated from the web source or seeded from app-local defaults?
- Should track config identity become a dedicated `trackConfigId` once the scraper can provide stable config IDs?
