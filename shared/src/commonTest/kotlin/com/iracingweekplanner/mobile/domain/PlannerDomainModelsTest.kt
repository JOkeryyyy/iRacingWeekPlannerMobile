package com.iracingweekplanner.mobile.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class PlannerDomainModelsTest {

    @Test
    fun plannerRaceModelUsesPlannerConcepts() {
        val raceWindow = TimeWindow(
            startsAt = Instant.parse("2026-06-16T00:00:00Z"),
            endsAt = Instant.parse("2026-06-23T00:00:00Z"),
        )
        val week = RaceWeek(
            number = RaceWeekNumber(1),
            window = raceWindow,
        )
        val series = PlannerSeries(
            id = SeriesId("formula-d"),
            name = "Formula D Fixed",
            category = SeriesCategory("Formula Car"),
            license = LicenseRequirement(className = "D", safetyRatingLevel = 4),
            setup = RaceSetup.FIXED,
            isOfficial = true,
        )
        val race = PlannerRace(
            id = RaceId("2026-s2-formula-d-w1-road-america"),
            seriesId = series.id,
            weekNumber = week.number,
            window = raceWindow,
            track = RaceTrackRef(
                id = TrackId("road-america"),
                name = "Road America",
                configurationName = "Full Course",
            ),
            carIds = listOf(CarId("fia-f4")),
            carClasses = listOf("FIA F4"),
            sessions = listOf(
                RaceSessionSchedule.Recurring(
                    firstSessionOffset = 60.minutes,
                    repeatEvery = 120.minutes,
                ),
            ),
            length = RaceLength(lapCount = 20),
            rainChance = RainChance(25.0),
        )

        val recurringSchedule = race.sessions.single() as RaceSessionSchedule.Recurring

        assertEquals(RaceWeekNumber(1), race.weekNumber)
        assertEquals(TrackId("road-america"), race.track.id)
        assertEquals(CarId("fia-f4"), race.carIds.single())
        assertEquals(60.minutes, recurringSchedule.firstSessionOffset)
        assertEquals(120.minutes, recurringSchedule.repeatEvery)
        assertEquals(20, race.length?.lapCount)
        assertEquals(25.0, race.rainChance?.percentage)
    }

    @Test
    fun catalogModelsUseStableOwnershipKeys() {
        val car = PlannerCar(
            id = CarId("mx-5-cup"),
            displayName = "Global Mazda MX-5 Cup",
            sourceCarId = 35,
            sourceSkuName = "Mazda MX-5 Cup",
            categories = setOf("Road"),
            carClasses = setOf("MX-5"),
            isFreeWithSubscription = true,
            imageUrl = null,
        )
        val track = PlannerTrack(
            id = TrackId("lime-rock-park"),
            displayName = "Lime Rock Park",
            sourceTrackIds = setOf(123),
            primaryType = TrackType.ROAD,
            supportedTypes = setOf(TrackType.ROAD),
            isDefaultContent = true,
            mapUrl = "https://example.test/lime-rock-map.png",
            imageUrl = null,
        )

        assertEquals(CarId("mx-5-cup"), car.id)
        assertEquals(TrackId("lime-rock-park"), track.id)
        assertEquals(setOf(TrackType.ROAD), track.supportedTypes)
    }

    @Test
    fun plannerFiltersSeparateActiveFiltersFromLocalContentPreferences() {
        val filters = PlannerFilters(
            carOwnership = ContentOwnershipFilter.OWNED,
            trackOwnership = ContentOwnershipFilter.OWNED,
            favorites = FavoriteFilter.FAVORITES_ONLY,
            ownedCarIds = setOf(CarId("mx-5-cup")),
            favoriteCarIds = setOf(CarId("fia-f4")),
            ownedTrackIds = setOf(TrackId("lime-rock-park")),
            favoriteTrackIds = setOf(TrackId("road-america")),
            selectedSeriesIds = setOf(SeriesId("formula-d")),
            selectedCategories = setOf(SeriesCategory("Formula Car")),
        )

        assertEquals(ContentOwnershipFilter.OWNED, filters.carOwnership)
        assertEquals(FavoriteFilter.FAVORITES_ONLY, filters.favorites)
        assertEquals(setOf(CarId("mx-5-cup")), filters.ownedCarIds)
        assertEquals(setOf(TrackId("road-america")), filters.favoriteTrackIds)
    }

    @Test
    fun modelValueObjectsRejectInvalidPlannerData() {
        assertFailsWith<IllegalArgumentException> { RaceWeekNumber(0) }
        assertFailsWith<IllegalArgumentException> {
            TimeWindow(
                startsAt = Instant.parse("2026-06-23T00:00:00Z"),
                endsAt = Instant.parse("2026-06-16T00:00:00Z"),
            )
        }
        assertFailsWith<IllegalArgumentException> { RaceLength() }
        assertFailsWith<IllegalArgumentException> { RainChance(101.0) }
        assertFailsWith<IllegalArgumentException> {
            RaceSessionSchedule.Recurring(
                firstSessionOffset = 60.minutes,
                repeatEvery = (-1).minutes,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            RaceSessionSchedule.SetTimes(offsetsFromRaceStart = emptyList())
        }
    }
}
