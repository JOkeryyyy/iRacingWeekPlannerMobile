package com.iracingweekplanner.mobile.presentation

import com.iracingweekplanner.mobile.domain.model.CarId
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataError
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.repository.PlannerDataRepository
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.model.RaceId
import com.iracingweekplanner.mobile.domain.model.RaceLength
import com.iracingweekplanner.mobile.domain.model.RaceSessionSchedule
import com.iracingweekplanner.mobile.domain.model.RaceTrackRef
import com.iracingweekplanner.mobile.domain.model.RaceWeek
import com.iracingweekplanner.mobile.domain.model.RaceWeekNumber
import com.iracingweekplanner.mobile.domain.model.RainChance
import com.iracingweekplanner.mobile.domain.model.SeriesId
import com.iracingweekplanner.mobile.domain.model.TimeWindow
import com.iracingweekplanner.mobile.domain.model.TrackId
import com.iracingweekplanner.mobile.domain.model.PlannerTrack
import com.iracingweekplanner.mobile.domain.model.TrackType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class PlannerDataStateHolderTest {

    @Test
    fun startsIdleAndShowsLoadingBeforeLoadCompletes() = runBlocking {
        val loadStarted = CompletableDeferred<Unit>()
        val releaseLoad = CompletableDeferred<PlannerDataResult<PlannerData>>()
        val repository = FakePlannerDataRepository {
            loadStarted.complete(Unit)
            releaseLoad.await()
        }
        val stateHolder = PlannerDataStateHolder(LoadPlannerDataUseCase(repository))

        assertEquals(PlannerDataUiState.Idle, stateHolder.uiState.value)

        val loadJob = launch {
            stateHolder.onAction(PlannerDataAction.Load)
        }
        loadStarted.await()

        assertEquals(PlannerDataUiState.Loading, stateHolder.uiState.value)

        releaseLoad.complete(PlannerDataResult.Loaded(samplePlannerData()))
        loadJob.join()
    }

    @Test
    fun freshPlannerDataBecomesLoadedState() = runBlocking {
        val plannerData = samplePlannerData()
        val stateHolder = stateHolderFor(
            PlannerDataResult.Loaded(plannerData, PlannerDataFreshness.FRESH),
        )

        stateHolder.onAction(PlannerDataAction.Load)

        val loaded = stateHolder.uiState.value as PlannerDataUiState.Loaded
        assertEquals(plannerData.raceWeeks, loaded.raceWeeks)
        assertEquals(plannerData.plannerRaces, loaded.plannerRaces)
        assertEquals(plannerData.cars, loaded.cars)
        assertEquals(plannerData.tracks, loaded.tracks)
        assertEquals(PlannerDataFreshness.FRESH, loaded.freshness)
        assertEquals(false, loaded.isCached)
        assertNull(loaded.message)
    }

    @Test
    fun cachedPlannerDataBecomesLoadedStateWithRefreshWarning() = runBlocking {
        val plannerData = samplePlannerData()
        val stateHolder = stateHolderFor(
            PlannerDataResult.Loaded(plannerData, PlannerDataFreshness.CACHED),
        )

        stateHolder.onAction(PlannerDataAction.Load)

        val loaded = stateHolder.uiState.value as PlannerDataUiState.Loaded
        assertEquals(PlannerDataFreshness.CACHED, loaded.freshness)
        assertEquals(true, loaded.isCached)
        assertEquals(PlannerDataUiMessage.SHOWING_CACHED_PLANNER_DATA, loaded.message)
    }

    @Test
    fun emptyPlannerDataBecomesEmptyState() = runBlocking {
        val stateHolder = stateHolderFor(
            PlannerDataResult.Loaded(
                samplePlannerData(plannerRaces = emptyList()),
                PlannerDataFreshness.FRESH,
            ),
        )

        stateHolder.onAction(PlannerDataAction.Load)

        assertEquals(
            PlannerDataUiState.Empty(
                freshness = PlannerDataFreshness.FRESH,
                message = null,
            ),
            stateHolder.uiState.value,
        )
    }

    @Test
    fun sourceUnavailableWithoutCacheBecomesPresentationSafeError() = runBlocking {
        val stateHolder = stateHolderFor(
            PlannerDataResult.Failure(
                PlannerDataError.SourceUnavailable(
                    path = "manifest.json",
                    detail = "HTTP 500 should not become UI text",
                ),
            ),
        )

        stateHolder.onAction(PlannerDataAction.Load)

        assertEquals(
            PlannerDataUiState.Error(PlannerDataUiMessage.PLANNER_DATA_UNAVAILABLE),
            stateHolder.uiState.value,
        )
    }

    @Test
    fun invalidSourceDataWithoutCacheBecomesPresentationSafeError() = runBlocking {
        val stateHolder = stateHolderFor(
            PlannerDataResult.Failure(
                PlannerDataError.InvalidSourceData(
                    path = "season.races[0].startsAt",
                    detail = "not-a-timestamp should not become UI text",
                ),
            ),
        )

        stateHolder.onAction(PlannerDataAction.Load)

        assertEquals(
            PlannerDataUiState.Error(PlannerDataUiMessage.INVALID_PLANNER_DATA),
            stateHolder.uiState.value,
        )
    }

    @Test
    fun localStoreFailureBecomesPresentationSafeError() = runBlocking {
        val stateHolder = stateHolderFor(
            PlannerDataResult.Failure(
                PlannerDataError.LocalStoreFailure(
                    operation = PlannerDataError.LocalStoreOperation.READ,
                ),
            ),
        )

        stateHolder.onAction(PlannerDataAction.Load)

        assertEquals(
            PlannerDataUiState.Error(PlannerDataUiMessage.LOCAL_PLANNER_DATA_UNAVAILABLE),
            stateHolder.uiState.value,
        )
    }

    @Test
    fun retryRunsAnotherLogicalPlannerDataLoad() = runBlocking {
        val results = mutableListOf<PlannerDataResult<PlannerData>>(
            PlannerDataResult.Failure(
                PlannerDataError.SourceDecodeFailed(
                    path = "season.json",
                    detail = "malformed source details should not become UI text",
                ),
            ),
            PlannerDataResult.Loaded(samplePlannerData(), PlannerDataFreshness.FRESH),
        )
        val repository = FakePlannerDataRepository {
            results.removeAt(0)
        }
        val stateHolder = PlannerDataStateHolder(LoadPlannerDataUseCase(repository))

        stateHolder.onAction(PlannerDataAction.Load)
        stateHolder.onAction(PlannerDataAction.Retry)

        val loaded = stateHolder.uiState.value as PlannerDataUiState.Loaded
        assertEquals(2, repository.loadCount)
        assertEquals(PlannerDataFreshness.FRESH, loaded.freshness)
    }

    private fun stateHolderFor(
        result: PlannerDataResult<PlannerData>,
    ): PlannerDataStateHolder =
        PlannerDataStateHolder(
            LoadPlannerDataUseCase(
                FakePlannerDataRepository { result },
            ),
        )

    private class FakePlannerDataRepository(
        private val load: suspend () -> PlannerDataResult<PlannerData>,
    ) : PlannerDataRepository {
        var loadCount = 0
            private set

        override suspend fun loadPlannerData(): PlannerDataResult<PlannerData> {
            loadCount += 1
            return load()
        }
    }

    private fun samplePlannerData(
        raceWeeks: List<RaceWeek> = listOf(sampleRaceWeek(number = 1)),
        plannerRaces: List<PlannerRace> = listOf(samplePlannerRace(id = "race-1")),
        cars: List<PlannerCar> = listOf(
            PlannerCar(
                id = CarId("mx-5-cup"),
                displayName = "Global Mazda MX-5 Cup",
                sourceCarId = 35,
            ),
        ),
        tracks: List<PlannerTrack> = listOf(
            PlannerTrack(
                id = TrackId("lime-rock-park"),
                displayName = "Lime Rock Park",
                sourceTrackIds = setOf(123),
                primaryType = TrackType.ROAD,
            ),
        ),
    ): PlannerData =
        PlannerData(
            raceWeeks = raceWeeks,
            plannerRaces = plannerRaces,
            cars = cars,
            tracks = tracks,
        )

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
}
