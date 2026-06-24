package com.iracingweekplanner.mobile.data.mapper

import com.iracingweekplanner.mobile.data.dto.CarDto
import com.iracingweekplanner.mobile.data.dto.CarsCatalogDto
import com.iracingweekplanner.mobile.data.dto.LicenseDto
import com.iracingweekplanner.mobile.data.dto.RaceDto
import com.iracingweekplanner.mobile.data.dto.RaceLengthDto
import com.iracingweekplanner.mobile.data.dto.RaceSessionDto
import com.iracingweekplanner.mobile.data.dto.SeasonDto
import com.iracingweekplanner.mobile.data.dto.SeasonWeekDto
import com.iracingweekplanner.mobile.data.dto.SeriesDto
import com.iracingweekplanner.mobile.data.dto.TrackDto
import com.iracingweekplanner.mobile.data.dto.TracksCatalogDto
import com.iracingweekplanner.mobile.domain.model.CarId
import com.iracingweekplanner.mobile.domain.model.PlannerDataError
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.RaceId
import com.iracingweekplanner.mobile.domain.model.RaceSessionSchedule
import com.iracingweekplanner.mobile.domain.model.RaceSetup
import com.iracingweekplanner.mobile.domain.model.RaceWeekNumber
import com.iracingweekplanner.mobile.domain.model.SeasonId
import com.iracingweekplanner.mobile.domain.model.SeriesCategory
import com.iracingweekplanner.mobile.domain.model.SeriesId
import com.iracingweekplanner.mobile.domain.model.TrackId
import com.iracingweekplanner.mobile.domain.model.TrackType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class PlannerDataMappersTest {

    @Test
    fun mapsSeasonDtoIntoPlannerDomainModel() {
        val season = assertLoaded(sampleSeasonDto().toDomain())

        assertEquals(SeasonId("2026-s2"), season.id)
        assertEquals("2026 Season 2", season.name)
        assertEquals(Instant.parse("2026-03-17T00:00:00Z"), season.window.startsAt)
        assertEquals(Instant.parse("2026-06-09T00:00:00Z"), season.window.endsAt)
        assertEquals(Instant.parse("2026-03-17T00:00:00Z"), season.weekCalculationStartsAt)
        assertEquals(RaceWeekNumber(1), season.weeks.single().number)

        val series = season.series.single()
        assertEquals(SeriesId("series-global-mazda-mx5"), series.id)
        assertEquals("Global Mazda MX-5 Cup", series.name)
        assertEquals(SeriesCategory("Sports Car"), series.category)
        assertEquals("Rookie", series.license.className)
        assertEquals(1, series.license.safetyRatingLevel)
        assertEquals(RaceSetup.FIXED, series.setup)
        assertTrue(series.isOfficial)

        val recurringRace = season.races.first { it.id == RaceId("race-recurring") }
        val recurringSchedule = recurringRace.sessions.single() as RaceSessionSchedule.Recurring
        assertEquals(SeriesId("series-global-mazda-mx5"), recurringRace.seriesId)
        assertEquals(RaceWeekNumber(1), recurringRace.weekNumber)
        assertEquals(TrackId("okayama-international-circuit"), recurringRace.track.id)
        assertEquals("Okayama International Circuit", recurringRace.track.name)
        assertEquals("Full Course", recurringRace.track.configurationName)
        assertEquals(listOf(CarId("mazda-mx5-cup")), recurringRace.carIds)
        assertEquals(listOf("Global Mazda MX-5 Cup"), recurringRace.carClasses)
        assertEquals(10, recurringRace.length?.lapCount)
        assertNull(recurringRace.length?.timeLimitMinutes)
        assertEquals(0.0, recurringRace.rainChance?.percentage)
        assertEquals(60.minutes, recurringSchedule.firstSessionOffset)
        assertEquals(120.minutes, recurringSchedule.repeatEvery)

        val setTimesRace = season.races.first { it.id == RaceId("race-set-times") }
        val setTimesSchedule = setTimesRace.sessions.single() as RaceSessionSchedule.SetTimes
        assertEquals(30, setTimesRace.length?.timeLimitMinutes)
        assertNull(setTimesRace.length?.lapCount)
        assertEquals(listOf(120.minutes, 480.minutes, 840.minutes), setTimesSchedule.offsetsFromRaceStart)
    }

    @Test
    fun mapsCatalogDtosIntoPlannerDomainModels() {
        val cars = assertLoaded(
            CarsCatalogDto(
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
            ).toDomain(),
        )
        val tracks = assertLoaded(
            TracksCatalogDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                tracks = listOf(
                    TrackDto(
                        packageId = "daytona-international-speedway",
                        displayName = "Daytona International Speedway",
                        sourceTrackIds = listOf(1201, 1202),
                        type = "road",
                        supportedTypes = listOf("road", "oval"),
                        isDefaultContent = false,
                        mapUrl = "https://example.test/daytona-map.png",
                        imageUrl = "https://example.test/daytona.png",
                    ),
                ),
            ).toDomain(),
        )

        val car = cars.single()
        assertEquals(CarId("mazda-mx5-cup"), car.id)
        assertEquals("Global Mazda MX-5 Cup", car.displayName)
        assertEquals(1, car.sourceCarId)
        assertEquals("Mazda MX-5 Cup", car.sourceSkuName)
        assertEquals(setOf("Sports Car", "Road"), car.categories)
        assertEquals(setOf("Global Mazda MX-5 Cup"), car.carClasses)
        assertTrue(car.isFreeWithSubscription == true)
        assertEquals("https://example.test/mx5.png", car.imageUrl)

        val track = tracks.single()
        assertEquals(TrackId("daytona-international-speedway"), track.id)
        assertEquals(setOf(1201, 1202), track.sourceTrackIds)
        assertEquals(TrackType.ROAD, track.primaryType)
        assertEquals(setOf(TrackType.ROAD, TrackType.OVAL), track.supportedTypes)
        assertTrue(track.isDefaultContent == false)
        assertEquals("https://example.test/daytona-map.png", track.mapUrl)
        assertEquals("https://example.test/daytona.png", track.imageUrl)
    }

    @Test
    fun mapsAllowedMissingOptionalFieldsToNullOrEmptyDomainValues() {
        val race = sampleRaceDto(
            raceId = "race-with-minimum-optionals",
            trackConfigName = null,
            raceLength = null,
            precipChance = null,
        )
        val season = assertLoaded(sampleSeasonDto(races = listOf(race)).toDomain())
        val mappedRace = season.races.single()
        val cars = assertLoaded(
            CarsCatalogDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                cars = listOf(
                    CarDto(
                        sku = "formula-vee",
                        displayName = "Formula Vee",
                    ),
                ),
            ).toDomain(),
        )
        val tracks = assertLoaded(
            TracksCatalogDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                tracks = listOf(
                    TrackDto(
                        packageId = "charlotte-motor-speedway",
                        displayName = "Charlotte Motor Speedway",
                        sourceTrackIds = listOf(1301),
                    ),
                ),
            ).toDomain(),
        )

        assertNull(mappedRace.track.configurationName)
        assertNull(mappedRace.length)
        assertNull(mappedRace.rainChance)
        assertEquals(emptySet(), cars.single().categories)
        assertEquals(emptySet(), cars.single().carClasses)
        assertNull(cars.single().isFreeWithSubscription)
        assertNull(tracks.single().primaryType)
        assertEquals(emptySet(), tracks.single().supportedTypes)
    }

    @Test
    fun invalidRequiredTimestampReturnsPlannerDataFailure() {
        val result = sampleSeasonDto(
            seasonStart = "not-a-timestamp",
        ).toDomain()

        val error = assertInvalidSourceData(result)
        assertEquals("seasonStart", error.path)
    }

    @Test
    fun unknownRequiredSessionTypeReturnsPlannerDataFailure() {
        val result = sampleSeasonDto(
            races = listOf(
                sampleRaceDto(
                    sessions = listOf(RaceSessionDto(type = "practiceOnly")),
                ),
            ),
        ).toDomain()

        val error = assertInvalidSourceData(result)
        assertEquals("races[race-recurring].sessions[0].type", error.path)
    }

    @Test
    fun missingRequiredRecurringSessionFieldsReturnPlannerDataFailure() {
        val result = sampleSeasonDto(
            races = listOf(
                sampleRaceDto(
                    sessions = listOf(
                        RaceSessionDto(
                            type = "recurring",
                            firstSessionOffsetMinutes = 60,
                        ),
                    ),
                ),
            ),
        ).toDomain()

        val error = assertInvalidSourceData(result)
        assertEquals("races[race-recurring].sessions[0].repeatEveryMinutes", error.path)
    }

    @Test
    fun emptySetTimesSessionOffsetsReturnPlannerDataFailure() {
        val result = sampleSeasonDto(
            races = listOf(
                sampleRaceDto(
                    sessions = listOf(
                        RaceSessionDto(
                            type = "setTimes",
                            offsetMinutes = emptyList(),
                        ),
                    ),
                ),
            ),
        ).toDomain()

        val error = assertInvalidSourceData(result)
        assertEquals("races[race-recurring].sessions[0].offsetMinutes", error.path)
    }

    @Test
    fun unknownOptionalTrackTypesStillMapToNullOrSupportedKnownValues() {
        val track = assertLoaded(
            TracksCatalogDto(
                schemaVersion = 1,
                generatedAt = "2026-06-16T00:00:00Z",
                tracks = listOf(
                    TrackDto(
                        packageId = "test-track",
                        displayName = "Test Track",
                        sourceTrackIds = listOf(1),
                        type = "drag",
                        supportedTypes = listOf("road", "drag"),
                    ),
                ),
            ).toDomain(),
        ).single()

        assertNull(track.primaryType)
        assertEquals(setOf(TrackType.ROAD), track.supportedTypes)
    }

    private fun <T> assertLoaded(result: PlannerDataResult<T>): T {
        val loaded = result as PlannerDataResult.Loaded
        assertEquals(PlannerDataFreshness.FRESH, loaded.freshness)
        return loaded.data
    }

    private fun assertInvalidSourceData(result: PlannerDataResult<*>): PlannerDataError.InvalidSourceData =
        (result as PlannerDataResult.Failure).error as PlannerDataError.InvalidSourceData

    private fun sampleSeasonDto(
        seasonId: String = "2026-s2",
        seasonStart: String = "2026-03-17T00:00:00Z",
        seasonEnd: String = "2026-06-09T00:00:00Z",
        races: List<RaceDto> = listOf(
            sampleRaceDto(raceId = "race-recurring"),
            sampleRaceDto(
                raceId = "race-set-times",
                raceLength = RaceLengthDto(minutes = 30),
                sessions = listOf(
                    RaceSessionDto(
                        type = "setTimes",
                        offsetMinutes = listOf(120, 480, 840),
                    ),
                ),
            ),
        ),
    ): SeasonDto =
        SeasonDto(
            schemaVersion = 1,
            generatedAt = "2026-06-16T00:00:00Z",
            seasonId = seasonId,
            seasonName = "2026 Season 2",
            seasonStart = seasonStart,
            seasonEnd = seasonEnd,
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
            races = races,
        )

    private fun sampleRaceDto(
        raceId: String = "race-recurring",
        trackConfigName: String? = "Full Course",
        raceLength: RaceLengthDto? = RaceLengthDto(laps = 10),
        precipChance: Double? = 0.0,
        sessions: List<RaceSessionDto> = listOf(
            RaceSessionDto(
                type = "recurring",
                firstSessionOffsetMinutes = 60,
                repeatEveryMinutes = 120,
            ),
        ),
    ): RaceDto =
        RaceDto(
            raceId = raceId,
            seriesId = "series-global-mazda-mx5",
            weekNumber = 1,
            startsAt = "2026-03-17T00:00:00Z",
            endsAt = "2026-03-24T00:00:00Z",
            trackPackageId = "okayama-international-circuit",
            trackName = "Okayama International Circuit",
            carSkus = listOf("mazda-mx5-cup"),
            carClasses = listOf("Global Mazda MX-5 Cup"),
            sessions = sessions,
            trackConfigName = trackConfigName,
            raceLength = raceLength,
            precipChance = precipChance,
        )

}
