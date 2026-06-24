package com.iracingweekplanner.mobile.data.datasource

import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@RunWith(RobolectricTestRunner::class)
class ComposeResourcePlannerLocalDataSourceFixtureTest {

    private val fixtureDirectory = repoRoot()
        .resolve("shared/src/commonMain/composeResources/files/mock-data")

    @Test
    fun loadsAllMockFixtureDtosThroughDataSourceApi() = runBlocking {
        val source = ComposeResourcePlannerLocalDataSource(
            readText = { path -> fixtureDirectory.resolve(path).readText() },
        )

        val bundle = assertLoaded(source.loadPlannerData())
        assertEquals(1, bundle.manifest.schemaVersion)
        assertEquals("2026-s2", bundle.manifest.seasonId)
        assertEquals("season.json", bundle.manifest.seasonFile)
        assertEquals("cars.json", bundle.manifest.carsFile)
        assertEquals("tracks.json", bundle.manifest.tracksFile)
        assertEquals("2026 Season 2", bundle.season.seasonName)
        assertEquals(2, bundle.season.weeks.size)
        assertEquals(4, bundle.season.races.size)
        assertEquals(4, bundle.cars.cars.size)
        assertEquals(4, bundle.tracks.tracks.size)
    }

    @Test
    fun defaultSourceLoadsMockFixtureDtosFromComposeResources() = runBlocking {
        val bundle = assertLoaded(ComposeResourcePlannerLocalDataSource().loadPlannerData())

        assertEquals("2026-s2", bundle.manifest.seasonId)
        assertEquals("2026 Season 2", bundle.season.seasonName)
        assertEquals(4, bundle.cars.cars.size)
        assertEquals(4, bundle.tracks.tracks.size)
    }

    private fun assertLoaded(result: PlannerDataSourceResult): PlannerDataBundle =
        when (result) {
            is PlannerDataSourceResult.Loaded -> result.bundle
            is PlannerDataSourceResult.Failure -> fail("Expected loaded data, got $result")
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
