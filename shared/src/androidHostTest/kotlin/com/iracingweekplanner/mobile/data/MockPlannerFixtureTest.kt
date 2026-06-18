package com.iracingweekplanner.mobile.data

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockPlannerFixtureTest {

    private val fixtureDirectory = repoRoot()
        .resolve("shared/src/commonMain/composeResources/files/mock-data")
    private val preferenceFixtureDirectory = repoRoot()
        .resolve("shared/src/commonTest/resources/mock-data")

    @Test
    fun localMockDataMatchesDocumentedContractAndFilterScenarios() {
        val manifestFile = fixtureDirectory.resolve("manifest.json")
        val seasonFile = fixtureDirectory.resolve("season.json")
        val carsFile = fixtureDirectory.resolve("cars.json")
        val tracksFile = fixtureDirectory.resolve("tracks.json")
        val preferencesFile = preferenceFixtureDirectory.resolve("local-preferences.json")

        listOf(manifestFile, seasonFile, carsFile, tracksFile, preferencesFile).forEach { file ->
            assertTrue(file.isFile, "Missing fixture: ${file.path}")
        }

        val manifest = manifestFile.parseObject()
        val season = seasonFile.parseObject()
        val cars = carsFile.parseObject()
        val tracks = tracksFile.parseObject()
        val preferences = preferencesFile.parseObject()

        assertCommonMetadata(manifest)
        assertCommonMetadata(season, expectedSeasonId = "2026-s2")
        assertCommonMetadata(cars)
        assertCommonMetadata(tracks)

        assertEquals("2026-s2", manifest.requiredString("seasonId"))
        assertEquals("season.json", manifest.requiredString("seasonFile"))
        assertEquals("cars.json", manifest.requiredString("carsFile"))
        assertEquals("tracks.json", manifest.requiredString("tracksFile"))

        val weekNumbers = season.requiredArray("weeks")
            .map { it.jsonObject.requiredInt("weekNumber") }
            .toSet()
        val seriesIds = season.requiredArray("series")
            .map { it.jsonObject.requiredString("seriesId") }
            .toSet()
        val races = season.requiredArray("races").map { it.jsonObject }

        assertTrue(weekNumbers.isNotEmpty(), "Fixture must include at least one race week.")
        assertTrue(races.isNotEmpty(), "Fixture must include at least one race.")

        val carSkus = cars.requiredArray("cars")
            .map { it.jsonObject.requiredString("sku") }
            .toSet()
        val trackPackageIds = tracks.requiredArray("tracks")
            .map { it.jsonObject.requiredString("packageId") }
            .toSet()

        assertTrue(carSkus.size >= 3, "Fixture must include multiple cars for filter scenarios.")
        assertTrue(trackPackageIds.size >= 3, "Fixture must include multiple tracks for filter scenarios.")

        races.forEach { race ->
            assertTrue(race.requiredString("seriesId") in seriesIds)
            assertTrue(race.requiredInt("weekNumber") in weekNumbers)
            assertTrue(race.requiredString("trackPackageId") in trackPackageIds)
            assertTrue(race.requiredStringArray("carSkus").all { it in carSkus })
            assertTrue(race.requiredArray("sessions").isNotEmpty())
        }

        val ownedCars = preferences.requiredStringArray("ownedCarSkus").toSet()
        val favoriteCars = preferences.requiredStringArray("favoriteCarSkus").toSet()
        val ownedTracks = preferences.requiredStringArray("ownedTrackPackageIds").toSet()
        val favoriteTracks = preferences.requiredStringArray("favoriteTrackPackageIds").toSet()

        assertTrue(carSkus.containsAll(ownedCars + favoriteCars))
        assertTrue(trackPackageIds.containsAll(ownedTracks + favoriteTracks))
        assertTrue(races.any { race ->
            race.requiredString("trackPackageId") in ownedTracks &&
                race.requiredStringArray("carSkus").any { it in ownedCars }
        }, "Expected a race that can match owned car and owned track filters.")
        assertTrue(races.any { race ->
            race.requiredString("trackPackageId") in favoriteTracks ||
                race.requiredStringArray("carSkus").any { it in favoriteCars }
        }, "Expected a race that can match favorite car or favorite track filters.")
        assertTrue(races.any { race ->
            race.requiredString("trackPackageId") !in ownedTracks ||
                race.requiredStringArray("carSkus").none { it in ownedCars }
        }, "Expected a race that can be excluded by owned filters.")

        listOf(manifestFile, seasonFile, carsFile, tracksFile).forEach { file ->
            val text = file.readText()
            assertFalse(text.contains("ownedCars"), "${file.name} must not store owned cars.")
            assertFalse(text.contains("ownedCarSkus"), "${file.name} must not store owned car SKUs.")
            assertFalse(text.contains("ownedTracks"), "${file.name} must not store owned tracks.")
            assertFalse(text.contains("ownedTrackPackageIds"), "${file.name} must not store owned track IDs.")
            assertFalse(text.contains("favoriteCars"), "${file.name} must not store favorite cars.")
            assertFalse(text.contains("favoriteCarSkus"), "${file.name} must not store favorite car SKUs.")
            assertFalse(text.contains("favoriteTracks"), "${file.name} must not store favorite tracks.")
            assertFalse(text.contains("favoriteTrackPackageIds"), "${file.name} must not store favorite track IDs.")
        }
    }

    private fun assertCommonMetadata(json: JsonObject, expectedSeasonId: String? = null) {
        assertEquals(1, json.requiredInt("schemaVersion"))
        assertTrue(json.requiredString("generatedAt").endsWith("Z"))
        if (expectedSeasonId != null) {
            assertEquals(expectedSeasonId, json.requiredString("seasonId"))
        }
    }

    private fun File.parseObject(): JsonObject =
        Json.parseToJsonElement(readText()).jsonObject

    private fun JsonObject.requiredArray(name: String): JsonArray =
        assertNotNull(this[name] as? JsonArray, "Missing array field: $name")

    private fun JsonObject.requiredString(name: String): String =
        assertNotNull(this[name]?.jsonPrimitive?.contentOrNull, "Missing string field: $name")

    private fun JsonObject.requiredInt(name: String): Int =
        assertNotNull(this[name]?.jsonPrimitive?.intOrNull, "Missing integer field: $name")

    private fun JsonObject.requiredStringArray(name: String): List<String> =
        requiredArray(name).map { element ->
            assertNotNull(element.jsonPrimitive.contentOrNull, "$name must contain only strings.")
        }

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
