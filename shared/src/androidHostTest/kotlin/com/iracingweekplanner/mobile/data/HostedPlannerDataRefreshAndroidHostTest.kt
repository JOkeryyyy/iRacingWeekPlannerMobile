package com.iracingweekplanner.mobile.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.iracingweekplanner.mobile.data.datasource.KtorPlannerHostedDataSource
import com.iracingweekplanner.mobile.data.datasource.PlannerDataSource
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import com.iracingweekplanner.mobile.data.local.SqlDelightPlannerLocalDataStore
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataCoordinator
import com.iracingweekplanner.mobile.data.repository.RefreshCachePlannerDataRepository
import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataError
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HostedPlannerDataRefreshAndroidHostTest {

    @Test
    fun hostedSuccessPersistsThroughSqlDelightRefreshCachePath() = runBlocking {
        val database = createInMemoryPlannerDatabase()
        val repository = hostedRepository(
            source = hostedSource(
                httpClient = hostedSuccessClient(revision = "fresh"),
            ),
            database = database,
        )

        val loaded = assertLoaded(repository.loadPlannerData())

        assertEquals(PlannerDataFreshness.FRESH, loaded.freshness)
        assertEquals("Hosted Series", loaded.data.series.single().name)
        assertEquals("Hosted Track", loaded.data.tracks.single().displayName)

        val cached = assertLoaded(
            hostedRepository(
                source = hostedSource(httpClient = hostedFailureClient()),
                database = database,
            ).loadPlannerData(),
        )
        assertEquals(PlannerDataFreshness.CACHED, cached.freshness)
        assertEquals("Hosted Series", cached.data.series.single().name)
        assertEquals("Hosted Track", cached.data.tracks.single().displayName)
    }

    @Test
    fun hostedRefreshFailureUsesLastSuccessfulSqlDelightDataset() = runBlocking {
        val database = createInMemoryPlannerDatabase()
        val firstLoad = hostedRepository(
            source = hostedSource(
                httpClient = hostedSuccessClient(revision = "cached-revision"),
            ),
            database = database,
        )
        assertLoaded(firstLoad.loadPlannerData())

        val secondLoad = hostedRepository(
            source = hostedSource(httpClient = hostedFailureClient()),
            database = database,
        )

        val loaded = assertLoaded(secondLoad.loadPlannerData())

        assertEquals(PlannerDataFreshness.CACHED, loaded.freshness)
        assertEquals("Hosted Series", loaded.data.series.single().name)
        assertEquals("car-hosted", loaded.data.cars.single().id.value)
    }

    @Test
    fun hostedRefreshFailureWithoutCacheReturnsSourceUnavailable() = runBlocking {
        val repository = hostedRepository(
            source = hostedSource(httpClient = hostedFailureClient()),
            database = createInMemoryPlannerDatabase(),
        )

        val failure = assertFailure(repository.loadPlannerData())

        assertEquals(
            PlannerDataError.SourceUnavailable(
                path = "$BaseUrl/manifest.json",
                detail = "Source resource is unavailable",
            ),
            failure.error,
        )
    }

    private fun hostedRepository(
        source: PlannerDataSource,
        database: PlannerDatabase,
    ): RefreshCachePlannerDataRepository =
        RefreshCachePlannerDataRepository(
            coordinator = RefreshCachePlannerDataCoordinator(
                source = source,
                localDataStore = SqlDelightPlannerLocalDataStore(database),
            ),
        )

    private fun hostedSource(httpClient: HttpClient): KtorPlannerHostedDataSource =
        KtorPlannerHostedDataSource(
            manifestUrl = "$BaseUrl/manifest.json",
            httpClient = httpClient,
        )

    private fun hostedSuccessClient(revision: String): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.toString()) {
                        "$BaseUrl/manifest.json" -> respondJson(manifestJson(revision))
                        "$BaseUrl/releases/2026-s3/$revision/season.json" -> respondJson(SeasonJson)
                        "$BaseUrl/releases/2026-s3/$revision/cars.json" -> respondJson(CarsJson)
                        "$BaseUrl/releases/2026-s3/$revision/tracks.json" -> respondJson(TracksJson)
                        else -> respondError(HttpStatusCode.NotFound)
                    }
                }
            }
        }

    private fun hostedFailureClient(): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler {
                    respondError(HttpStatusCode.ServiceUnavailable)
                }
            }
        }

    private fun createInMemoryPlannerDatabase(): PlannerDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PlannerDatabase.Schema.create(driver)
        return PlannerDatabase(driver)
    }

    private fun MockRequestHandleScope.respondJson(
        content: String,
    ) = respond(
        content = content,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private fun assertLoaded(
        result: PlannerDataResult<PlannerData>,
    ): PlannerDataResult.Loaded<PlannerData> =
        when (result) {
            is PlannerDataResult.Loaded -> result
            is PlannerDataResult.Failure -> fail("Expected loaded planner data, got $result")
        }

    private fun assertFailure(
        result: PlannerDataResult<PlannerData>,
    ): PlannerDataResult.Failure =
        when (result) {
            is PlannerDataResult.Failure -> result
            is PlannerDataResult.Loaded -> fail("Expected planner data failure, got $result")
        }

    private companion object {
        const val BaseUrl =
            "https://project-ref.supabase.co/storage/v1/object/public/planner-data/data/mobile/v1"

        fun manifestJson(revision: String): String =
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-26T15:05:47Z",
              "seasonId": "2026-s3",
              "revision": "$revision",
              "seasonFile": "releases/2026-s3/$revision/season.json",
              "carsFile": "releases/2026-s3/$revision/cars.json",
              "tracksFile": "releases/2026-s3/$revision/tracks.json"
            }
            """.trimIndent()

        val SeasonJson =
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-26T15:05:47Z",
              "seasonId": "2026-s3",
              "seasonName": "2026 Season 3",
              "seasonStart": "2026-06-16T00:00:00Z",
              "seasonEnd": "2026-09-08T00:00:00Z",
              "weekSeasonStart": "2026-06-16T00:00:00Z",
              "weeks": [
                {
                  "weekNumber": 1,
                  "startsAt": "2026-06-16T00:00:00Z",
                  "endsAt": "2026-06-23T00:00:00Z"
                }
              ],
              "series": [
                {
                  "seriesId": "series-hosted",
                  "name": "Hosted Series",
                  "category": "Sports Car",
                  "license": {
                    "className": "Class D",
                    "safetyRating": 4.0,
                    "raw": "Class D 4.0"
                  },
                  "isOfficial": true,
                  "setupType": "fixed",
                  "setupSource": "parser",
                  "startType": "standing",
                  "startTypeSource": "parser",
                  "raceIds": [
                    "race-hosted-week-1"
                  ]
                }
              ],
              "races": [
                {
                  "raceId": "race-hosted-week-1",
                  "seriesId": "series-hosted",
                  "seriesName": "Hosted Series",
                  "category": "Sports Car",
                  "weekNumber": 1,
                  "startsAt": "2026-06-16T00:00:00Z",
                  "endsAt": "2026-06-23T00:00:00Z",
                  "trackPackageId": "track-hosted",
                  "trackName": "Hosted Track",
                  "trackConfigName": "GP",
                  "carSkus": [
                    "car-hosted"
                  ],
                  "carClasses": [
                    "Hosted Class"
                  ],
                  "setupType": "fixed",
                  "setupSource": "parser",
                  "startType": "standing",
                  "startTypeSource": "parser",
                  "raceLength": {
                    "laps": 10
                  },
                  "precipChance": 0,
                  "sessions": [
                    {
                      "type": "recurring",
                      "firstSessionOffsetMinutes": 15,
                      "repeatEveryMinutes": 60
                    }
                  ]
                }
              ]
            }
            """.trimIndent()

        val CarsJson =
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-26T15:05:47Z",
              "cars": [
                {
                  "sku": "car-hosted",
                  "displayName": "Hosted Car",
                  "sourceCarId": 101,
                  "sourceSkuName": "Hosted Car",
                  "categories": [
                    "Sports Car"
                  ],
                  "carClasses": [
                    "Hosted Class"
                  ],
                  "freeWithSubscription": true,
                  "imageUrl": "https://example.test/hosted-car.png"
                }
              ]
            }
            """.trimIndent()

        val TracksJson =
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-26T15:05:47Z",
              "tracks": [
                {
                  "packageId": "track-hosted",
                  "displayName": "Hosted Track",
                  "sourceTrackIds": [
                    201
                  ],
                  "type": "road",
                  "supportedTypes": [
                    "road"
                  ],
                  "isDefaultContent": true,
                  "mapUrl": "https://example.test/hosted-track-map.png",
                  "imageUrl": "https://example.test/hosted-track.png"
                }
              ]
            }
            """.trimIndent()
    }
}
