package com.iracingweekplanner.mobile.data.dto

import java.io.File
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MobileDataDtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = false
    }
    private val fixtureDirectory = repoRoot()
        .resolve("shared/src/commonMain/composeResources/files/mock-data")

    @Test
    fun parsesEveryHostedMockFixtureIntoDataLayerDtos() {
        val manifest = json.decodeFromString<MobileDataManifestDto>(fixture("manifest.json"))
        val season = json.decodeFromString<SeasonDto>(fixture("season.json"))
        val cars = json.decodeFromString<CarsCatalogDto>(fixture("cars.json"))
        val tracks = json.decodeFromString<TracksCatalogDto>(fixture("tracks.json"))

        assertEquals(1, manifest.schemaVersion)
        assertEquals("2026-06-16T00:00:00Z", manifest.generatedAt)
        assertEquals("2026-s2", manifest.seasonId)
        assertEquals("season.json", manifest.seasonFile)
        assertEquals("cars.json", manifest.carsFile)
        assertEquals("tracks.json", manifest.tracksFile)
        assertEquals("mock-2026-s2-001", manifest.revision)
        assertNull(manifest.checksums)

        assertEquals("2026 Season 2", season.seasonName)
        assertEquals("2026-03-17T00:00:00Z", season.seasonStart)
        assertEquals("2026-06-09T00:00:00Z", season.seasonEnd)
        assertEquals("2026-03-17T00:00:00Z", season.weekSeasonStart)
        assertEquals(2, season.weeks.size)
        assertEquals(4, season.series.size)
        assertEquals(4, season.races.size)

        val porscheCup = season.series.first { it.seriesId == "series-porsche-cup" }
        assertEquals("Porsche Cup", porscheCup.name)
        assertEquals("Sports Car", porscheCup.category)
        assertEquals("C", porscheCup.license.className)
        assertEquals(3, porscheCup.license.level)
        assertTrue(porscheCup.isOfficial)
        assertTrue(porscheCup.isFixedSetup)

        val timedRace = season.races.first { it.seriesId == "series-porsche-cup" }
        assertEquals("virginia-international-raceway", timedRace.trackPackageId)
        assertEquals("Full Course", timedRace.trackConfigName)
        assertEquals(listOf("porsche-911-gt3-cup-992"), timedRace.carSkus)
        assertEquals(listOf("Porsche 911 GT3 Cup"), timedRace.carClasses)
        assertEquals(30, timedRace.raceLength?.minutes)
        assertNull(timedRace.raceLength?.laps)
        assertEquals(15.0, timedRace.precipChance)
        assertEquals("setTimes", timedRace.sessions.single().type)
        assertEquals(listOf(120, 480, 840), timedRace.sessions.single().offsetMinutes)

        val recurringRace = season.races.first { it.seriesId == "series-global-mazda-mx5" }
        assertEquals(10, recurringRace.raceLength?.laps)
        assertEquals(0.0, recurringRace.precipChance)
        assertEquals("recurring", recurringRace.sessions.single().type)
        assertEquals(60, recurringRace.sessions.single().firstSessionOffsetMinutes)
        assertEquals(120, recurringRace.sessions.single().repeatEveryMinutes)

        val raceWithoutRain = season.races.first { it.seriesId == "series-formula-vee" }
        assertNull(raceWithoutRain.precipChance)

        assertEquals(4, cars.cars.size)
        val porsche = cars.cars.first { it.sku == "porsche-911-gt3-cup-992" }
        assertEquals("Porsche 911 GT3 Cup (992)", porsche.displayName)
        assertEquals(2, porsche.sourceCarId)
        assertEquals("Porsche 911 GT3 Cup", porsche.sourceSkuName)
        assertEquals(listOf("Sports Car", "Road"), porsche.categories)
        assertEquals(listOf("Porsche 911 GT3 Cup", "GT3 Cup"), porsche.carClasses)
        assertFalse(porsche.freeWithSubscription ?: true)
        assertNull(porsche.imageUrl)

        assertEquals(4, tracks.tracks.size)
        val daytona = tracks.tracks.first { it.packageId == "daytona-international-speedway" }
        assertEquals("Daytona International Speedway", daytona.displayName)
        assertEquals(listOf(1201, 1202), daytona.sourceTrackIds)
        assertEquals("road", daytona.type)
        assertEquals(listOf("road", "oval"), daytona.supportedTypes)
        assertFalse(daytona.isDefaultContent ?: true)
        assertNull(daytona.mapUrl)
        assertNull(daytona.imageUrl)
    }

    @Test
    fun parsesRepresentativeOptionalManifestChecksums() {
        val manifest = json.decodeFromString<MobileDataManifestDto>(
            """
            {
              "schemaVersion": 1,
              "generatedAt": "2026-06-16T00:00:00Z",
              "seasonId": "2026-s2",
              "seasonFile": "season.json",
              "carsFile": "cars.json",
              "tracksFile": "tracks.json",
              "checksums": {
                "season.json": "sha256:season",
                "cars.json": "sha256:cars",
                "tracks.json": "sha256:tracks"
              }
            }
            """.trimIndent()
        )

        assertNull(manifest.revision)
        assertEquals("sha256:season", manifest.checksums?.get("season.json"))
        assertEquals("sha256:cars", manifest.checksums?.get("cars.json"))
        assertEquals("sha256:tracks", manifest.checksums?.get("tracks.json"))
    }

    @Test
    fun failsWhenRequiredTopLevelFieldsAreMissing() {
        assertFailsWith<SerializationException> {
            json.decodeFromString<MobileDataManifestDto>(
                """
                {
                  "schemaVersion": 1,
                  "generatedAt": "2026-06-16T00:00:00Z",
                  "seasonId": "2026-s2",
                  "seasonFile": "season.json",
                  "carsFile": "cars.json"
                }
                """.trimIndent()
            )
        }
        assertFailsWith<SerializationException> {
            json.decodeFromString<SeasonDto>(
                """
                {
                  "schemaVersion": 1,
                  "generatedAt": "2026-06-16T00:00:00Z",
                  "seasonId": "2026-s2",
                  "seasonName": "2026 Season 2",
                  "seasonStart": "2026-03-17T00:00:00Z",
                  "seasonEnd": "2026-06-09T00:00:00Z",
                  "weekSeasonStart": "2026-03-17T00:00:00Z",
                  "weeks": [],
                  "series": []
                }
                """.trimIndent()
            )
        }
        assertFailsWith<SerializationException> {
            json.decodeFromString<CarsCatalogDto>(
                """
                {
                  "schemaVersion": 1,
                  "generatedAt": "2026-06-16T00:00:00Z"
                }
                """.trimIndent()
            )
        }
        assertFailsWith<SerializationException> {
            json.decodeFromString<TracksCatalogDto>(
                """
                {
                  "schemaVersion": 1,
                  "generatedAt": "2026-06-16T00:00:00Z"
                }
                """.trimIndent()
            )
        }
    }

    private fun fixture(name: String): String =
        fixtureDirectory.resolve(name).readText()

    private fun repoRoot(): File {
        val userDirectory = System.getProperty("user.dir") ?: error("user.dir is not available")
        val start = File(userDirectory).absoluteFile
        return generateSequence(start) { it.parentFile }
            .first { candidate ->
                candidate.resolve("settings.gradle.kts").isFile &&
                    candidate.resolve("shared").isDirectory
            }
    }
}
