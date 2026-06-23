# Hosted JSON Source Shape Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a source-agnostic planner data source interface and a tested Ktor-backed hosted JSON implementation for Sprint 2 Story 2.4.

**Architecture:** Generalize the existing local source contract to `PlannerDataSource` so local mock resources and hosted HTTP JSON return the same `PlannerDataSourceResult`. Keep manifest URL resolution, Ktor calls, and hosted reference validation inside the data layer. Do not wire Koin or repository refresh behavior in this story.

**Tech Stack:** Kotlin Multiplatform, kotlinx.serialization, Ktor Client 3.5.0, Ktor MockEngine, kotlin.test, kotlinx.coroutines `runBlocking`.

---

## File Structure

- Modify `gradle/libs.versions.toml`
  - Add `ktor-client-mock` alias using the existing `ktor` version.
- Modify `shared/build.gradle.kts`
  - Add `libs.ktor.client.mock` to `commonTest.dependencies`.
- Rename `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerLocalDataSource.kt` to `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerDataSource.kt`
  - Keep `PlannerDataBundle`, `PlannerDataSourceResult`, and `PlannerDataSourceFailure`.
  - Rename `PlannerLocalDataSource` to `PlannerDataSource`.
  - Add `PlannerDataSourceFailure.Reason.INVALID_REFERENCE`.
- Modify `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSource.kt`
  - Implement `PlannerDataSource`.
- Create `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSource.kt`
  - Load manifest URL, validate relative references, fetch referenced files, decode DTOs, and return typed source results.
- Modify `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceTest.kt`
  - Keep existing behavior tests green after the interface rename.
- Create `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSourceTest.kt`
  - Cover hosted success, HTTP failure, thrown request failure, decode failure, and invalid manifest references.

## Task 1: Add Ktor MockEngine Test Dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `shared/build.gradle.kts`

- [ ] **Step 1: Add the version-catalog alias**

In `gradle/libs.versions.toml`, add this library entry beside `ktor-client-core`:

```toml
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktor" }
```

- [ ] **Step 2: Add the common test dependency**

In `shared/build.gradle.kts`, update `commonTest.dependencies`:

```kotlin
commonTest.dependencies {
    implementation(libs.kotlin.test)
    implementation(libs.koin.test)
    implementation(libs.ktor.client.mock)
}
```

- [ ] **Step 3: Verify dependency resolution**

Run:

```bash
./gradlew :shared:compileTestKotlinMetadata
```

Expected: the task completes successfully or proceeds far enough that `libs.ktor.client.mock` resolves. If it fails, the failure should not be an unresolved version-catalog alias.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts
git commit -m "test: add Ktor mock engine dependency"
```

## Task 2: Generalize the Existing Data Source Interface

**Files:**
- Rename: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerLocalDataSource.kt` to `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerDataSource.kt`
- Modify: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSource.kt`
- Modify: `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceTest.kt`
- Check: `shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceFixtureTest.kt`
- Check: `shared/src/iosSimulatorArm64Test/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceIosResourceTest.kt`

- [ ] **Step 1: Rename the source contract file**

Run:

```bash
git mv shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerLocalDataSource.kt shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerDataSource.kt
```

- [ ] **Step 2: Replace the file contents**

Use this full content for `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerDataSource.kt`:

```kotlin
package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.MobileDataManifestDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto

data class PlannerDataBundle(
    val manifest: MobileDataManifestDto,
    val season: SeasonDto,
    val cars: CarsCatalogDto,
    val tracks: TracksCatalogDto,
)

sealed interface PlannerDataSourceResult {
    data class Loaded(
        val bundle: PlannerDataBundle,
    ) : PlannerDataSourceResult

    data class Failure(
        val failure: PlannerDataSourceFailure,
    ) : PlannerDataSourceResult
}

data class PlannerDataSourceFailure(
    val path: String,
    val reason: Reason,
    val detail: String,
) {
    enum class Reason {
        RESOURCE_UNAVAILABLE,
        DECODE_FAILED,
        INVALID_REFERENCE,
    }
}

interface PlannerDataSource {
    suspend fun loadPlannerData(): PlannerDataSourceResult
}
```

- [ ] **Step 3: Update the local source implementation**

In `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSource.kt`, change the class declaration to:

```kotlin
class ComposeResourcePlannerLocalDataSource(
    private val json: Json = Json {
        ignoreUnknownKeys = false
    },
    private val readText: suspend (String) -> String = ::readMockResourceText,
) : PlannerDataSource {
```

- [ ] **Step 4: Verify no stale interface references remain**

Run:

```bash
rg "PlannerLocalDataSource|PlannerLocalDataSource.kt"
```

Expected: no output.

- [ ] **Step 5: Run focused local source tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests '*ComposeResourcePlannerLocalDataSource*'
```

Expected: the local data-source tests pass.

- [ ] **Step 6: Commit**

```bash
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerDataSource.kt shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSource.kt shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceTest.kt shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceFixtureTest.kt shared/src/iosSimulatorArm64Test/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceIosResourceTest.kt
git add -u shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerLocalDataSource.kt
git commit -m "refactor: generalize planner data source contract"
```

## Task 3: Write Hosted Source Tests

**Files:**
- Create: `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSourceTest.kt`

- [ ] **Step 1: Create the failing hosted source test file**

Create `shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSourceTest.kt` with this content:

```kotlin
package com.iracingweekplanner.mobile.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class KtorPlannerHostedDataSourceTest {

    @Test
    fun loadsManifestAndRelativePlannerFiles() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val client = httpClient { url ->
            requestedUrls += url
            when (url) {
                "$BaseUrl/manifest.json" -> respondJson(ManifestJson)
                "$BaseUrl/season.json" -> respondJson(SeasonJson)
                "$BaseUrl/catalog/cars.json" -> respondJson(CarsJson)
                "$BaseUrl/catalog/tracks.json" -> respondJson(TracksJson)
                else -> respondError(HttpStatusCode.NotFound)
            }
        }
        val source = KtorPlannerHostedDataSource(
            manifestUrl = "$BaseUrl/manifest.json",
            httpClient = client,
        )

        val bundle = assertLoaded(source.loadPlannerData())

        assertEquals(
            listOf(
                "$BaseUrl/manifest.json",
                "$BaseUrl/season.json",
                "$BaseUrl/catalog/cars.json",
                "$BaseUrl/catalog/tracks.json",
            ),
            requestedUrls,
        )
        assertEquals("2026-s2", bundle.manifest.seasonId)
        assertEquals("2026 Season 2", bundle.season.seasonName)
        assertEquals("car-1", bundle.cars.cars.single().sku)
        assertEquals("track-1", bundle.tracks.tracks.single().packageId)
    }

    @Test
    fun returnsUnavailableWhenHttpStatusIsNotSuccess() = runBlocking {
        val source = KtorPlannerHostedDataSource(
            manifestUrl = "$BaseUrl/manifest.json",
            httpClient = httpClient { respondError(HttpStatusCode.InternalServerError) },
        )

        val failure = assertFailure(source.loadPlannerData())

        assertEquals("$BaseUrl/manifest.json", failure.path)
        assertEquals(PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE, failure.reason)
        assertTrue(failure.detail.contains("500"))
    }

    @Test
    fun returnsUnavailableWhenRequestThrows() = runBlocking {
        val source = KtorPlannerHostedDataSource(
            manifestUrl = "$BaseUrl/manifest.json",
            httpClient = HttpClient(MockEngine) {
                engine {
                    addHandler {
                        error("network down")
                    }
                }
            },
        )

        val failure = assertFailure(source.loadPlannerData())

        assertEquals("$BaseUrl/manifest.json", failure.path)
        assertEquals(PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE, failure.reason)
        assertTrue(failure.detail.contains("network down"))
    }

    @Test
    fun returnsDecodeFailureForInvalidJson() = runBlocking {
        val source = KtorPlannerHostedDataSource(
            manifestUrl = "$BaseUrl/manifest.json",
            httpClient = httpClient { url ->
                when (url) {
                    "$BaseUrl/manifest.json" -> respondJson("{")
                    else -> respondError(HttpStatusCode.NotFound)
                }
            },
        )

        val failure = assertFailure(source.loadPlannerData())

        assertEquals("$BaseUrl/manifest.json", failure.path)
        assertEquals(PlannerDataSourceFailure.Reason.DECODE_FAILED, failure.reason)
    }

    @Test
    fun returnsInvalidReferenceForUnsafeManifestPaths() = runBlocking {
        val unsafeManifests = listOf(
            ManifestJson.replace("\"season.json\"", "\"https://evil.example/season.json\""),
            ManifestJson.replace("\"season.json\"", "\"/data/mobile/v1/season.json\""),
            ManifestJson.replace("\"season.json\"", "\"../season.json\""),
            ManifestJson.replace("\"season.json\"", "\"\""),
        )

        unsafeManifests.forEach { manifestJson ->
            val source = KtorPlannerHostedDataSource(
                manifestUrl = "$BaseUrl/manifest.json",
                httpClient = httpClient { respondJson(manifestJson) },
            )

            val failure = assertFailure(source.loadPlannerData())

            assertEquals(PlannerDataSourceFailure.Reason.INVALID_REFERENCE, failure.reason)
            assertEquals("seasonFile", failure.path)
        }
    }

    private fun httpClient(handler: (String) -> io.ktor.client.engine.mock.MockResponseData): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    handler(request.url.toString())
                }
            }
        }

    private fun respondJson(content: String): io.ktor.client.engine.mock.MockResponseData =
        respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )

    private fun assertLoaded(result: PlannerDataSourceResult): PlannerDataBundle =
        when (result) {
            is PlannerDataSourceResult.Loaded -> result.bundle
            is PlannerDataSourceResult.Failure -> fail("Expected hosted data to load, got $result")
        }

    private fun assertFailure(result: PlannerDataSourceResult): PlannerDataSourceFailure =
        when (result) {
            is PlannerDataSourceResult.Failure -> result.failure
            is PlannerDataSourceResult.Loaded -> fail("Expected hosted data-source failure, got $result")
        }

    private companion object {
        const val BaseUrl = "https://example.com/data/mobile/v1"

        val ManifestJson = """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-16T00:00:00Z",
              "seasonId": "2026-s2",
              "seasonFile": "season.json",
              "carsFile": "catalog/cars.json",
              "tracksFile": "catalog/tracks.json"
            }
        """.trimIndent()

        val SeasonJson = """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-16T00:00:00Z",
              "seasonId": "2026-s2",
              "seasonName": "2026 Season 2",
              "seasonStart": "2026-03-17T00:00:00Z",
              "seasonEnd": "2026-06-09T00:00:00Z",
              "weekSeasonStart": "2026-03-17T00:00:00Z",
              "weeks": [
                {
                  "weekNumber": 1,
                  "startsAt": "2026-03-17T00:00:00Z",
                  "endsAt": "2026-03-24T00:00:00Z"
                }
              ],
              "series": [
                {
                  "seriesId": "series-1",
                  "name": "Series One",
                  "category": "Sports Car",
                  "license": {
                    "className": "Rookie",
                    "level": 1
                  },
                  "isOfficial": true,
                  "isFixedSetup": true
                }
              ],
              "races": [
                {
                  "raceId": "race-1",
                  "seriesId": "series-1",
                  "weekNumber": 1,
                  "startsAt": "2026-03-17T00:00:00Z",
                  "endsAt": "2026-03-24T00:00:00Z",
                  "trackPackageId": "track-1",
                  "trackName": "Track One",
                  "carSkus": [
                    "car-1"
                  ],
                  "carClasses": [
                    "Class One"
                  ],
                  "sessions": [
                    {
                      "type": "recurring",
                      "firstSessionOffsetMinutes": 60,
                      "repeatEveryMinutes": 120
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val CarsJson = """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-16T00:00:00Z",
              "cars": [
                {
                  "sku": "car-1",
                  "displayName": "Car One"
                }
              ]
            }
        """.trimIndent()

        val TracksJson = """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-16T00:00:00Z",
              "tracks": [
                {
                  "packageId": "track-1",
                  "displayName": "Track One",
                  "sourceTrackIds": [
                    1
                  ]
                }
              ]
            }
        """.trimIndent()
    }
}
```

- [ ] **Step 2: Run the failing hosted source tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests '*KtorPlannerHostedDataSourceTest*'
```

Expected: FAIL because `KtorPlannerHostedDataSource` does not exist yet.

- [ ] **Step 3: Commit the failing tests**

```bash
git add shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSourceTest.kt
git commit -m "test: cover hosted planner data source shape"
```

## Task 4: Implement the Hosted Ktor Data Source

**Files:**
- Create: `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSource.kt`

- [ ] **Step 1: Add the implementation**

Create `shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSource.kt`:

```kotlin
package com.iracingweekplanner.mobile.data

import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.MobileDataManifestDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class KtorPlannerHostedDataSource(
    private val manifestUrl: String,
    private val httpClient: HttpClient,
    private val json: Json = Json {
        ignoreUnknownKeys = false
    },
) : PlannerDataSource {

    override suspend fun loadPlannerData(): PlannerDataSourceResult {
        val manifest = loadDto<MobileDataManifestDto>(manifestUrl)
        if (manifest is DecodeResult.Failure) return manifest.toSourceFailure()
        manifest as DecodeResult.Success

        val seasonUrl = resolveManifestReference(manifest.data.seasonFile)
            ?: return invalidReference("seasonFile", manifest.data.seasonFile)
        val carsUrl = resolveManifestReference(manifest.data.carsFile)
            ?: return invalidReference("carsFile", manifest.data.carsFile)
        val tracksUrl = resolveManifestReference(manifest.data.tracksFile)
            ?: return invalidReference("tracksFile", manifest.data.tracksFile)

        val season = loadDto<SeasonDto>(seasonUrl)
        if (season is DecodeResult.Failure) return season.toSourceFailure()
        season as DecodeResult.Success

        val cars = loadDto<CarsCatalogDto>(carsUrl)
        if (cars is DecodeResult.Failure) return cars.toSourceFailure()
        cars as DecodeResult.Success

        val tracks = loadDto<TracksCatalogDto>(tracksUrl)
        if (tracks is DecodeResult.Failure) return tracks.toSourceFailure()
        tracks as DecodeResult.Success

        return PlannerDataSourceResult.Loaded(
            PlannerDataBundle(
                manifest = manifest.data,
                season = season.data,
                cars = cars.data,
                tracks = tracks.data,
            ),
        )
    }

    private suspend inline fun <reified T> loadDto(url: String): DecodeResult<T> {
        val text = try {
            val response = httpClient.get(url)
            if (!response.status.isSuccess()) {
                return DecodeResult.Failure(
                    PlannerDataSourceFailure(
                        path = url,
                        reason = PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE,
                        detail = "HTTP ${response.status.value} ${response.status.description}",
                    ),
                )
            }
            response.body<String>()
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            return DecodeResult.Failure(
                PlannerDataSourceFailure(
                    path = url,
                    reason = PlannerDataSourceFailure.Reason.RESOURCE_UNAVAILABLE,
                    detail = error.message.orEmpty(),
                ),
            )
        }

        return try {
            DecodeResult.Success(json.decodeFromString(text))
        } catch (error: SerializationException) {
            DecodeResult.Failure(
                PlannerDataSourceFailure(
                    path = url,
                    reason = PlannerDataSourceFailure.Reason.DECODE_FAILED,
                    detail = error.message.orEmpty(),
                ),
            )
        } catch (error: IllegalArgumentException) {
            DecodeResult.Failure(
                PlannerDataSourceFailure(
                    path = url,
                    reason = PlannerDataSourceFailure.Reason.DECODE_FAILED,
                    detail = error.message.orEmpty(),
                ),
            )
        }
    }

    private fun resolveManifestReference(relativePath: String): String? {
        val trimmed = relativePath.trim()
        if (!isSafeRelativePath(trimmed)) return null

        val baseUrl = manifestUrl.substringBeforeLast("/", missingDelimiterValue = "")
        if (baseUrl.isBlank()) return null

        return "$baseUrl/$trimmed"
    }

    private fun isSafeRelativePath(path: String): Boolean {
        if (path.isBlank()) return false
        if (path.startsWith("/") || path.startsWith("//")) return false
        if (path.contains("://")) return false

        val segments = path.split("/")
        return segments.all { segment ->
            segment.isNotBlank() && segment != "." && segment != ".."
        }
    }

    private fun invalidReference(fieldName: String, value: String): PlannerDataSourceResult.Failure =
        PlannerDataSourceResult.Failure(
            PlannerDataSourceFailure(
                path = fieldName,
                reason = PlannerDataSourceFailure.Reason.INVALID_REFERENCE,
                detail = "Invalid hosted manifest reference: $value",
            ),
        )

    private fun DecodeResult.Failure.toSourceFailure(): PlannerDataSourceResult.Failure =
        PlannerDataSourceResult.Failure(failure)

    private sealed interface DecodeResult<out T> {
        data class Success<out T>(
            val data: T,
        ) : DecodeResult<T>

        data class Failure(
            val failure: PlannerDataSourceFailure,
        ) : DecodeResult<Nothing>
    }
}
```

- [ ] **Step 2: Run hosted source tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest --tests '*KtorPlannerHostedDataSourceTest*'
```

Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSource.kt
git commit -m "feat: add hosted planner data source"
```

## Task 5: Run Story-Level Verification

**Files:**
- Check all changed Story 2.4 files.

- [ ] **Step 1: Check the working tree**

Run:

```bash
git status --short --branch
```

Expected: on the Story 2.4 branch, with only expected Story 2.4 files changed or committed.

- [ ] **Step 2: Check whitespace**

Run:

```bash
git diff --check
git diff --cached --check
```

Expected: both commands produce no output.

- [ ] **Step 3: Run shared host tests**

Run:

```bash
./gradlew :shared:testAndroidHostTest
```

Expected: PASS.

- [ ] **Step 4: Run Android assemble**

Run:

```bash
./gradlew :androidApp:assembleDebug
```

Expected: PASS.

- [ ] **Step 5: Run iOS simulator tests**

Run:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew :shared:iosSimulatorArm64Test
```

Expected: PASS. If local iOS tooling blocks the command, capture the exact failure and include it in the handoff.

- [ ] **Step 6: Final Story 2.4 commit if needed**

If verification changes require fixes, commit the final fix with a focused message:

```bash
git add gradle/libs.versions.toml shared/build.gradle.kts shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/PlannerDataSource.kt shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSource.kt shared/src/commonMain/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSource.kt shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceTest.kt shared/src/commonTest/kotlin/com/iracingweekplanner/mobile/data/KtorPlannerHostedDataSourceTest.kt shared/src/androidHostTest/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceFixtureTest.kt shared/src/iosSimulatorArm64Test/kotlin/com/iracingweekplanner/mobile/data/ComposeResourcePlannerLocalDataSourceIosResourceTest.kt
git commit -m "fix: align hosted data source verification"
```

## Self-Review Checklist

- Spec coverage:
  - Common source interface: Task 2.
  - Hosted Ktor implementation: Task 4.
  - Manifest-relative file loading: Tasks 3 and 4.
  - Fake/mock HTTP tests: Tasks 1 and 3.
  - No production URL: Task 4 constructor input and tests.
  - No DI/repository/cache/UI work: file structure and task scope exclude those files.
- Placeholder scan:
  - The plan contains no incomplete sections or undefined feature work.
- Type consistency:
  - `PlannerDataSource`, `PlannerDataSourceResult`, `PlannerDataSourceFailure`, and `PlannerDataBundle` are consistently named across tests and implementation.
  - `INVALID_REFERENCE` is added before hosted tests assert it.
  - `KtorPlannerHostedDataSource` constructor parameters match the tests and implementation.
