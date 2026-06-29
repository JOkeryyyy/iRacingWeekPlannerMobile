# Hosted JSON Android Cache Root Cause

Date investigated: 2026-06-28 to 2026-06-29

Audience: mobile agents, backend/static-data publisher owners, and reviewers working on Story 3.5 hosted JSON consumption.

## Summary

Android is showing old mock data, but not because the hosted API call is missing or because the Android build is still using the local mock flavor. The Android `hostedDevDebug` build reaches the configured Supabase manifest URL, downloads hosted `2026-s3` data, and maps the payload successfully.

The failure happens when the mapped hosted dataset is written to SQLDelight. The live hosted `season.json` contains duplicate `seriesId` values, while the mobile database schema requires `series.series_id` to be unique. SQLDelight rejects the write, so the refresh/cache coordinator falls back to the previous local cache. On the tested device, that previous cache was the bundled mock `2026-s2` dataset, so the UI kept showing old mock races with the cached-data banner.

## User-Visible Symptom

- Android UI shows Week 1 data from the previous mock schedule.
- UI displays the state panel: `Showing cached schedule data`.
- Device database remains on:
  - `season_id = 2026-s2`
  - `revision = mock-2026-s2-001`
  - `races = 4`
  - `cars = 4`
  - `tracks = 4`

## Confirmed Not the Cause

- Not wrong Android flavor: `hostedDevDebug` includes the Supabase manifest URL through `manifestPlaceholders["plannerHostedManifestUrl"]`.
- Not missing Android internet permission: the installed APK requests and has `android.permission.INTERNET`.
- Not hosted endpoint availability from the Mac: the manifest URL returned hosted data.
- Not DTO decode or common mapper failure: a temporary live diagnostic test loaded and mapped hosted data successfully.
- Not stale source edits: temporary diagnostics were removed and the working tree was clean after investigation.

## Failing Boundary

The mobile schema currently defines `series_id` as the primary key:

```sql
CREATE TABLE series (
  series_id TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  license_class_name TEXT NOT NULL,
  license_safety_rating_level INTEGER,
  setup TEXT NOT NULL,
  is_official INTEGER NOT NULL
);
```

Reference: `shared/src/commonMain/sqldelight/com/iracingweekplanner/mobile/data/db/PlannerLocalData.sq`.

The local store replaces the full dataset in one transaction:

```kotlin
database.transaction {
    clearPlannerDataset()
    insertDataset(dataset)
}
```

Reference: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/local/SqlDelightPlannerLocalDataStore.kt`.

When the hosted payload is inserted, Android logcat showed:

```text
SQLiteConstraintException: UNIQUE constraint failed: series.series_id
```

The coordinator then falls back to cached data after local write failure:

```kotlin
is PlannerLocalDataWriteResult.Failure -> loadCachedOrFailure(
    PlannerDataError.LocalStoreFailure(
        operation = PlannerDataError.LocalStoreOperation.WRITE,
    ),
)
```

Reference: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/repository/RefreshCachePlannerDataCoordinator.kt`.

## Hosted Payload Evidence

Configured manifest:

```text
https://ivuwegboyxrzucbfgzvh.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1/manifest.json
```

Observed manifest metadata:

```text
seasonId = 2026-s3
revision = b973e3f6
generatedAt = 2026-06-26T15:05:47Z
```

Temporary live diagnostic test result:

```text
mapped season=2026-s3 revision=b973e3f6
mapped weeks=9 races=1328
mapped cars=82 tracks=221
```

Duplicate hosted series ID scan:

```text
series_count = 148
unique_series_ids = 131
duplicate_id_count = 17
```

Duplicate `seriesId` values observed:

```text
series_big_block_modified_series_2026_s3
series_cars_tour_late_model_stocks_2026_s3
series_dirt_midget_cup_2026_s3
series_dirtcar_360_sprint_car_series_2026_s3
series_dirtcar_pro_late_model_series_2026_s3
series_dirtcar_ump_modified_series_2026_s3
series_formula_a_cosworth_cup_grand_prix_2026_s3
series_formula_b_super_formula_series_2026_s3
series_formula_c_dallara_f3_series_2026_s3
series_imsa_iracing_series_2026_s3
series_indycar_series_2026_s3
series_nascar_class_a_series_2026_s3
series_nascar_tour_modified_series_2026_s3
series_super_late_model_series_2026_s3
series_tcr_virtual_challenge_2026_s3
series_world_of_outlaws_late_model_series_2026_s3
series_world_of_outlaws_sprint_car_series_2026_s3
```

The duplicates appear to represent distinct variants of the same named series, commonly differing by license/setup metadata such as `Unknown` versus `Class D` or `unknown` versus `fixed`.

## Backend / Static Publisher Action Needed

The backend/static JSON publisher should decide whether duplicate named series variants are expected.

If they are expected, each emitted `seriesId` must be globally unique for the mobile data contract. For example, include the distinguishing dimension in the generated ID, such as license class, setup type, server region, or another stable variant key.

If they are not expected, deduplicate the `series` array before publishing `season.json`, and ensure every `race.seriesId` still points to the canonical retained series.

Recommended backend validation before publish:

```text
For season.json:
- every series[].seriesId is unique
- every races[].seriesId exists in series[].seriesId
- every series[].raceIds entry exists in races[].raceId
- every races[].raceId is unique
```

## Mobile Follow-Up Options

Preferred fix: keep `series_id` unique and require the hosted contract to publish unique series IDs. This preserves the current mobile model and SQLDelight schema.

Alternative only if product semantics require duplicate `seriesId`: change the mobile domain/storage identity from `seriesId` to a variant-aware key, then update DTOs, mappers, SQL schema, and race references together. This is larger and should not be done as a silent mobile workaround.

Possible mobile diagnostics improvement: expose or log the write-failure reason in debug builds. The current UI correctly shows cached fallback, but it hides the concrete cause from developers.

## Verification Commands Used

Focused hosted tests on clean source:

```bash
./gradlew :shared:testAndroidHostTest \
  --tests com.iracingweekplanner.mobile.di.PlannerDataSourceSelectionTest \
  --tests com.iracingweekplanner.mobile.data.HostedPlannerDataRefreshAndroidHostTest \
  --tests com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSourceTest
```

Result: passed.

Clean hosted Android install after removing diagnostics:

```bash
./gradlew :androidApp:installHostedDevDebug
```

Result: installed successfully on the connected Android device.
