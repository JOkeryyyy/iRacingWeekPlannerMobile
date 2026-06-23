package com.iracingweekplanner.mobile.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.iracingweekplanner.mobile.data.db.PlannerDatabase
import com.iracingweekplanner.mobile.data.dto.CarDto
import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.LicenseDto
import com.iracingweekplanner.mobile.data.dto.MobileDataManifestDto
import com.iracingweekplanner.mobile.data.dto.RaceDto
import com.iracingweekplanner.mobile.data.dto.RaceLengthDto
import com.iracingweekplanner.mobile.data.dto.RaceSessionDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.SeasonWeekDto
import com.iracingweekplanner.mobile.data.dto.SeriesDto
import com.iracingweekplanner.mobile.data.dto.TrackDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto
import com.iracingweekplanner.mobile.domain.CarId
import com.iracingweekplanner.mobile.domain.RaceId
import com.iracingweekplanner.mobile.domain.RaceSessionSchedule
import com.iracingweekplanner.mobile.domain.RaceSetup
import com.iracingweekplanner.mobile.domain.RaceWeekNumber
import com.iracingweekplanner.mobile.domain.SeasonId
import com.iracingweekplanner.mobile.domain.SeriesCategory
import com.iracingweekplanner.mobile.domain.SeriesId
import com.iracingweekplanner.mobile.domain.TrackId
import com.iracingweekplanner.mobile.domain.TrackType
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class SqlDelightPlannerLocalDataStoreTest {

    @Test
    fun replaceIfValidPersistsFullPlannerDatasetAndReadReturnsDomainModels() = runBlocking {
        val store = createStore()

        assertEquals(true, store.replaceIfValid(sampleBundle(revision = "valid")))

        val dataset = store.read() ?: fail("Expected local planner dataset")
        assertEquals("valid", dataset.metadata.revision)
        assertEquals(1, dataset.metadata.schemaVersion)
        assertEquals("2026-06-16T00:00:00Z", dataset.metadata.generatedAt)
        assertEquals(SeasonId("2026-s2"), dataset.season.id)
        assertEquals("2026 Season 2", dataset.season.name)
        assertEquals(Instant.parse("2026-03-17T00:00:00Z"), dataset.season.window.startsAt)
        assertEquals(Instant.parse("2026-06-09T00:00:00Z"), dataset.season.window.endsAt)
        assertEquals(Instant.parse("2026-03-17T00:00:00Z"), dataset.season.weekCalculationStartsAt)
        assertEquals(RaceWeekNumber(1), dataset.season.weeks.single().number)

        val series = dataset.season.series.single()
        assertEquals(SeriesId("series-global-mazda-mx5"), series.id)
        assertEquals("Global Mazda MX-5 Cup", series.name)
        assertEquals(SeriesCategory("Sports Car"), series.category)
        assertEquals("Rookie", series.license.className)
        assertEquals(1, series.license.safetyRatingLevel)
        assertEquals(RaceSetup.FIXED, series.setup)
        assertTrue(series.isOfficial)

        val race = dataset.season.races.single()
        assertEquals(RaceId("race-global-mazda-week-1"), race.id)
        assertEquals(SeriesId("series-global-mazda-mx5"), race.seriesId)
        assertEquals(RaceWeekNumber(1), race.weekNumber)
        assertEquals(TrackId("okayama-international-circuit"), race.track.id)
        assertEquals("Okayama International Circuit", race.track.name)
        assertEquals("Full Course", race.track.configurationName)
        assertEquals(listOf(CarId("mazda-mx5-cup")), race.carIds)
        assertEquals(listOf("Global Mazda MX-5 Cup"), race.carClasses)
        assertEquals(10, race.length?.lapCount)
        assertNull(race.length?.timeLimitMinutes)
        assertEquals(0.0, race.rainChance?.percentage)
        val recurring = race.sessions.single() as RaceSessionSchedule.Recurring
        assertEquals(60.minutes, recurring.firstSessionOffset)
        assertEquals(120.minutes, recurring.repeatEvery)

        val car = dataset.cars.single()
        assertEquals(CarId("mazda-mx5-cup"), car.id)
        assertEquals("Global Mazda MX-5 Cup", car.displayName)
        assertEquals(1, car.sourceCarId)
        assertEquals("Mazda MX-5 Cup", car.sourceSkuName)
        assertEquals(setOf("Sports Car", "Road"), car.categories)
        assertEquals(setOf("Global Mazda MX-5 Cup"), car.carClasses)
        assertTrue(car.isFreeWithSubscription == true)
        assertEquals("https://example.test/mx5.png", car.imageUrl)

        val track = dataset.tracks.single()
        assertEquals(TrackId("okayama-international-circuit"), track.id)
        assertEquals("Okayama International Circuit", track.displayName)
        assertEquals(setOf(1, 2), track.sourceTrackIds)
        assertEquals(TrackType.ROAD, track.primaryType)
        assertEquals(setOf(TrackType.ROAD, TrackType.OVAL), track.supportedTypes)
        assertTrue(track.isDefaultContent == true)
        assertEquals("https://example.test/okayama-map.png", track.mapUrl)
        assertEquals("https://example.test/okayama.png", track.imageUrl)
    }

    @Test
    fun readReturnsNullWhenDatabaseIsEmpty() = runBlocking {
        val store = createStore()

        assertNull(store.read())
    }

    @Test
    fun replaceIfValidDoesNotOverwriteExistingDatasetWhenSourceDataIsInvalid() = runBlocking {
        val store = createStore()
        assertEquals(true, store.replaceIfValid(sampleBundle(revision = "valid")))

        assertEquals(
            false,
            store.replaceIfValid(
                sampleBundle(
                    revision = "invalid",
                    seasonStart = "not-a-timestamp",
                ),
            ),
        )
        assertEquals("valid", store.read()?.metadata?.revision)
    }

    private fun createStore(): SqlDelightPlannerLocalDataStore {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PlannerDatabase.Schema.create(driver)
        return SqlDelightPlannerLocalDataStore(PlannerDatabase(driver))
    }

    private fun sampleBundle(
        revision: String,
        seasonStart: String = "2026-03-17T00:00:00Z",
    ): PlannerDataBundle =
        PlannerDataBundle(
            manifest = MobileDataManifestDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                seasonId = "2026-s2",
                seasonFile = "season.json",
                carsFile = "cars.json",
                tracksFile = "tracks.json",
                revision = revision,
            ),
            season = SeasonDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                seasonId = "2026-s2",
                seasonName = "2026 Season 2",
                seasonStart = seasonStart,
                seasonEnd = "2026-06-09T00:00:00Z",
                weekSeasonStart = "2026-03-17T00:00:00Z",
                weeks = listOf(
                    SeasonWeekDto(
                        weekNumber = 1,
                        startsAt = "2026-03-17T00:00:00Z",
                        endsAt = "2026-03-24T00:00:00Z",
                    ),
                ),
                series = listOf(
                    SeriesDto(
                        seriesId = "series-global-mazda-mx5",
                        name = "Global Mazda MX-5 Cup",
                        category = "Sports Car",
                        license = LicenseDto(className = "Rookie", level = 1),
                        isOfficial = true,
                        isFixedSetup = true,
                    ),
                ),
                races = listOf(
                    RaceDto(
                        raceId = "race-global-mazda-week-1",
                        seriesId = "series-global-mazda-mx5",
                        weekNumber = 1,
                        startsAt = "2026-03-17T00:00:00Z",
                        endsAt = "2026-03-24T00:00:00Z",
                        trackPackageId = "okayama-international-circuit",
                        trackName = "Okayama International Circuit",
                        trackConfigName = "Full Course",
                        carSkus = listOf("mazda-mx5-cup"),
                        carClasses = listOf("Global Mazda MX-5 Cup"),
                        sessions = listOf(
                            RaceSessionDto(
                                type = "recurring",
                                firstSessionOffsetMinutes = 60,
                                repeatEveryMinutes = 120,
                            ),
                        ),
                        raceLength = RaceLengthDto(laps = 10),
                        precipChance = 0.0,
                    ),
                ),
            ),
            cars = CarsCatalogDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                cars = listOf(
                    CarDto(
                        sku = "mazda-mx5-cup",
                        displayName = "Global Mazda MX-5 Cup",
                        sourceCarId = 1,
                        sourceSkuName = "Mazda MX-5 Cup",
                        categories = listOf("Sports Car", "Road"),
                        carClasses = listOf("Global Mazda MX-5 Cup"),
                        freeWithSubscription = true,
                        imageUrl = "https://example.test/mx5.png",
                    ),
                ),
            ),
            tracks = TracksCatalogDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                tracks = listOf(
                    TrackDto(
                        packageId = "okayama-international-circuit",
                        displayName = "Okayama International Circuit",
                        sourceTrackIds = listOf(1, 2),
                        type = "road",
                        supportedTypes = listOf("road", "oval"),
                        isDefaultContent = true,
                        mapUrl = "https://example.test/okayama-map.png",
                        imageUrl = "https://example.test/okayama.png",
                    ),
                ),
            ),
        )
}
