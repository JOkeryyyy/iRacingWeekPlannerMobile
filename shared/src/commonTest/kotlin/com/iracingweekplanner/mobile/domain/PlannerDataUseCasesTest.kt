package com.iracingweekplanner.mobile.domain

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class PlannerDataUseCasesTest {

    @Test
    fun loadRaceWeeksReturnsRaceWeeksFromRepository() = runSuspending {
        val expectedWeeks = listOf(sampleRaceWeek(number = 1), sampleRaceWeek(number = 2))
        val repository = FakePlannerScheduleRepository(
            raceWeeks = PlannerDataResult.Loaded(expectedWeeks),
        )

        val actualWeeks = LoadRaceWeeksUseCase(repository)() as PlannerDataResult.Loaded

        assertEquals(expectedWeeks, actualWeeks.data)
        assertEquals(PlannerDataFreshness.FRESH, actualWeeks.freshness)
        assertEquals(1, repository.raceWeeksLoadCount)
        assertEquals(0, repository.racesLoadCount)
    }

    @Test
    fun loadPlannerRacesReturnsPlannerRacesFromRepository() = runSuspending {
        val expectedRaces = listOf(samplePlannerRace(id = "race-1"), samplePlannerRace(id = "race-2"))
        val repository = FakePlannerScheduleRepository(
            races = PlannerDataResult.Loaded(expectedRaces),
        )

        val actualRaces = LoadPlannerRacesUseCase(repository)() as PlannerDataResult.Loaded

        assertEquals(expectedRaces, actualRaces.data)
        assertEquals(PlannerDataFreshness.FRESH, actualRaces.freshness)
        assertEquals(0, repository.raceWeeksLoadCount)
        assertEquals(1, repository.racesLoadCount)
    }

    @Test
    fun loadPlannerRacesReturnsRepositoryFailure() = runSuspending {
        val expectedError = PlannerDataError.InvalidSourceData(
            path = "season.races[0].startsAt",
            detail = "Invalid timestamp",
        )
        val repository = FakePlannerScheduleRepository(
            races = PlannerDataResult.Failure(expectedError),
        )

        val actualRaces = LoadPlannerRacesUseCase(repository)()

        assertEquals(PlannerDataResult.Failure(expectedError), actualRaces)
        assertEquals(1, repository.racesLoadCount)
    }

    @Test
    fun loadPlannerCarsReturnsCarsFromRepository() = runSuspending {
        val expectedCars = listOf(
            PlannerCar(
                id = CarId("mx-5-cup"),
                displayName = "Global Mazda MX-5 Cup",
                sourceCarId = 35,
            ),
            PlannerCar(
                id = CarId("fia-f4"),
                displayName = "FIA F4",
                sourceCarId = 170,
            ),
        )
        val repository = FakePlannerCarRepository(
            cars = PlannerDataResult.Loaded(expectedCars),
        )

        val actualCars = LoadPlannerCarsUseCase(repository)() as PlannerDataResult.Loaded

        assertEquals(expectedCars, actualCars.data)
        assertEquals(PlannerDataFreshness.FRESH, actualCars.freshness)
        assertEquals(1, repository.loadCount)
    }

    @Test
    fun loadPlannerTracksReturnsTracksFromRepository() = runSuspending {
        val expectedTracks = listOf(
            PlannerTrack(
                id = TrackId("lime-rock-park"),
                displayName = "Lime Rock Park",
                sourceTrackIds = setOf(123),
                primaryType = TrackType.ROAD,
            ),
            PlannerTrack(
                id = TrackId("charlotte-motor-speedway"),
                displayName = "Charlotte Motor Speedway",
                sourceTrackIds = setOf(456),
                primaryType = TrackType.OVAL,
            ),
        )
        val repository = FakePlannerTrackRepository(
            tracks = PlannerDataResult.Loaded(expectedTracks),
        )

        val actualTracks = LoadPlannerTracksUseCase(repository)() as PlannerDataResult.Loaded

        assertEquals(expectedTracks, actualTracks.data)
        assertEquals(PlannerDataFreshness.FRESH, actualTracks.freshness)
        assertEquals(1, repository.loadCount)
    }

    private class FakePlannerScheduleRepository(
        private val raceWeeks: PlannerDataResult<List<RaceWeek>> = PlannerDataResult.Loaded(emptyList()),
        private val races: PlannerDataResult<List<PlannerRace>> = PlannerDataResult.Loaded(emptyList()),
    ) : PlannerScheduleRepository {
        var raceWeeksLoadCount = 0
            private set
        var racesLoadCount = 0
            private set

        override suspend fun loadRaceWeeks(): PlannerDataResult<List<RaceWeek>> {
            raceWeeksLoadCount += 1
            return raceWeeks
        }

        override suspend fun loadPlannerRaces(): PlannerDataResult<List<PlannerRace>> {
            racesLoadCount += 1
            return races
        }
    }

    private class FakePlannerCarRepository(
        private val cars: PlannerDataResult<List<PlannerCar>>,
    ) : PlannerCarRepository {
        var loadCount = 0
            private set

        override suspend fun loadPlannerCars(): PlannerDataResult<List<PlannerCar>> {
            loadCount += 1
            return cars
        }
    }

    private class FakePlannerTrackRepository(
        private val tracks: PlannerDataResult<List<PlannerTrack>>,
    ) : PlannerTrackRepository {
        var loadCount = 0
            private set

        override suspend fun loadPlannerTracks(): PlannerDataResult<List<PlannerTrack>> {
            loadCount += 1
            return tracks
        }
    }

    private fun sampleRaceWeek(number: Int): RaceWeek {
        val offset = ((number - 1) * 7 * 24 * 60).minutes
        return RaceWeek(
            number = RaceWeekNumber(number),
            window = TimeWindow(
                startsAt = Instant.parse("2026-03-17T00:00:00Z") + offset,
                endsAt = Instant.parse("2026-03-24T00:00:00Z") + offset,
            ),
        )
    }

    private fun samplePlannerRace(id: String): PlannerRace =
        PlannerRace(
            id = RaceId(id),
            seriesId = SeriesId("series-global-mazda-mx5"),
            weekNumber = RaceWeekNumber(1),
            window = TimeWindow(
                startsAt = Instant.parse("2026-03-17T00:00:00Z"),
                endsAt = Instant.parse("2026-03-24T00:00:00Z"),
            ),
            track = RaceTrackRef(
                id = TrackId("okayama-international-circuit"),
                name = "Okayama International Circuit",
                configurationName = "Full Course",
            ),
            carIds = listOf(CarId("mazda-mx5-cup")),
            carClasses = listOf("Global Mazda MX-5 Cup"),
            sessions = listOf(
                RaceSessionSchedule.Recurring(
                    firstSessionOffset = 60.minutes,
                    repeatEvery = 120.minutes,
                ),
            ),
            length = RaceLength(lapCount = 10),
            rainChance = RainChance(0.0),
        )

    private fun runSuspending(block: suspend () -> Unit) {
        var failure: Throwable? = null
        block.startCoroutine(
            object : Continuation<Unit> {
                override val context: CoroutineContext = EmptyCoroutineContext

                override fun resumeWith(result: Result<Unit>) {
                    failure = result.exceptionOrNull()
                }
            },
        )
        failure?.let { throw it }
    }
}
