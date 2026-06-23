package com.iracingweekplanner.mobile.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpResponseData
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

    private fun httpClient(handler: MockRequestHandleScope.(String) -> HttpResponseData): HttpClient =
        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    handler(request.url.toString())
                }
            }
        }

    private fun MockRequestHandleScope.respondJson(content: String): HttpResponseData =
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
