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

The MVP planner needs these fields after the source audit. Exact JSON names are finalized in Story 1.2.

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

### Open Questions

- Should hosted mobile `season.json` contain planner-ready race rows, or raw web series/tracks plus derivation metadata? Planner-ready rows are recommended for v1.
- Should the mobile contract preserve `catid` and license numeric fields, normalized display labels, or both?
- Does the mobile MVP need precipitation chance/rain display, or should `precipChance` wait for a later UI story?
- Should free/default catalog flags be included in hosted `cars.json` and `tracks.json` to seed local ownership defaults, or should the mobile app ship its own default preference seed?
- Should track config identity be represented as a dedicated `trackConfigId` once the scraper can provide it, or is `pkgid` plus config/display name enough for MVP?
- How should null car class/car mapping data be represented when the scraper cannot resolve a class or car? The web scraper can produce nulls in intermediate class data.
- What hosted path will distinguish the mobile data manifest from the web PWA manifest?
