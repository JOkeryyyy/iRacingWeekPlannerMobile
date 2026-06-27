package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.domain.model.CarId
import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerData
import com.iracingweekplanner.mobile.domain.model.PlannerDataError
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.PlannerRace
import com.iracingweekplanner.mobile.domain.model.PlannerTrack
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
import com.iracingweekplanner.mobile.domain.model.TrackType
import com.iracingweekplanner.mobile.domain.repository.PlannerDataRepository
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsLoadingUntilInitialPlannerDataLoadCompletes() = runTest {
        val loadStarted = CompletableDeferred<Unit>()
        val releaseLoad = CompletableDeferred<PlannerDataResult<PlannerData>>()
        val repository = FakePlannerDataRepository {
            loadStarted.complete(Unit)
            releaseLoad.await()
        }
        val viewModel = viewModelFor(repository)

        viewModel.onAction(ScheduleAction.InitialLoad)
        loadStarted.await()

        assertTrue(viewModel.state.value.isLoading)

        releaseLoad.complete(PlannerDataResult.Loaded(samplePlannerData(), PlannerDataFreshness.FRESH))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun loadedPlannerDataDerivesSelectedWeekScheduleUiState() = runTest {
        val viewModel = viewModelFor(
            PlannerDataResult.Loaded(
                samplePlannerData(
                    raceWeeks = listOf(sampleRaceWeek(12), sampleRaceWeek(13)),
                    plannerRaces = listOf(
                        samplePlannerRace(id = "race-12", weekNumber = 12),
                        samplePlannerRace(id = "race-13", weekNumber = 13),
                    ),
                ),
                PlannerDataFreshness.FRESH,
            ),
            currentWeekNumber = { 13 },
        )

        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(13, state.selectedWeekNumber)
        assertNull(state.lastUpdatedDisplayText)
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertFalse(state.isCached)
        assertNull(state.panelMessage)
        assertEquals(listOf("race-13"), state.raceCards.map { it.raceId })
    }

    @Test
    fun cachedPlannerDataPreservesDisplayDataAndExposesCachedStatus() = runTest {
        val viewModel = viewModelFor(
            PlannerDataResult.Loaded(
                samplePlannerData(
                    raceWeeks = listOf(sampleRaceWeek(13)),
                    plannerRaces = listOf(samplePlannerRace(id = "race-13", weekNumber = 13)),
                ),
                PlannerDataFreshness.CACHED,
            ),
            currentWeekNumber = { 13 },
        )

        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isCached)
        assertEquals(listOf("race-13"), state.raceCards.map { it.raceId })
        assertEquals(ScheduleUiMessage.ShowingCachedPlannerData, state.panelMessage)
    }

    @Test
    fun emptyPlannerDataRequestsEmptyPanelInsteadOfBlankRaceList() = runTest {
        val viewModel = viewModelFor(
            PlannerDataResult.Loaded(
                samplePlannerData(plannerRaces = emptyList()),
                PlannerDataFreshness.FRESH,
            ),
            currentWeekNumber = { 13 },
        )

        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isEmpty)
        assertTrue(state.raceCards.isEmpty())
        assertNull(state.panelMessage)
    }

    @Test
    fun sourceErrorWithoutCacheRequestsRetryableErrorPanel() = runTest {
        val viewModel = viewModelFor(
            PlannerDataResult.Failure(
                PlannerDataError.SourceUnavailable(
                    path = "manifest.json",
                    detail = "HTTP 500 should not become UI text",
                ),
            ),
            currentWeekNumber = { 13 },
        )

        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(ScheduleUiMessage.PlannerDataUnavailable, state.panelMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertTrue(state.raceCards.isEmpty())
    }

    @Test
    fun invalidSourceDataRequestsInvalidDataPanel() = runTest {
        val viewModel = viewModelFor(
            PlannerDataResult.Failure(
                PlannerDataError.InvalidSourceData(
                    path = "season.races[0].startsAt",
                    detail = "not-a-timestamp should not become UI text",
                ),
            ),
        )

        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()

        assertEquals(ScheduleUiMessage.InvalidPlannerData, viewModel.state.value.panelMessage)
    }

    @Test
    fun localStoreFailureRequestsLocalDataUnavailablePanel() = runTest {
        val viewModel = viewModelFor(
            PlannerDataResult.Failure(
                PlannerDataError.LocalStoreFailure(
                    operation = PlannerDataError.LocalStoreOperation.READ,
                ),
            ),
        )

        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()

        assertEquals(ScheduleUiMessage.LocalPlannerDataUnavailable, viewModel.state.value.panelMessage)
    }

    @Test
    fun initialLoadRunsOnlyOnePlannerDataLoad() = runTest {
        val repository = FakePlannerDataRepository {
            PlannerDataResult.Loaded(samplePlannerData(), PlannerDataFreshness.FRESH)
        }
        val viewModel = viewModelFor(repository)

        viewModel.onAction(ScheduleAction.InitialLoad)
        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()

        assertEquals(1, repository.loadCount)
    }

    @Test
    fun refreshAndRetryEachRunOnePlannerDataLoad() = runTest {
        val repository = FakePlannerDataRepository {
            PlannerDataResult.Loaded(samplePlannerData(), PlannerDataFreshness.FRESH)
        }
        val viewModel = viewModelFor(repository)

        viewModel.onAction(ScheduleAction.Refresh)
        viewModel.onAction(ScheduleAction.Retry)
        advanceUntilIdle()

        assertEquals(2, repository.loadCount)
    }

    @Test
    fun weekSelectionActionsDoNotReloadPlannerData() = runTest {
        val repository = FakePlannerDataRepository {
            PlannerDataResult.Loaded(
                samplePlannerData(
                    raceWeeks = listOf(sampleRaceWeek(12), sampleRaceWeek(13), sampleRaceWeek(14)),
                    plannerRaces = listOf(
                        samplePlannerRace(id = "race-12", weekNumber = 12),
                        samplePlannerRace(id = "race-13", weekNumber = 13),
                        samplePlannerRace(id = "race-14", weekNumber = 14),
                    ),
                ),
                PlannerDataFreshness.FRESH,
            )
        }
        val viewModel = viewModelFor(
            repository = repository,
            currentWeekNumber = { 13 },
        )

        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()
        viewModel.onAction(ScheduleAction.NextWeek)
        advanceUntilIdle()
        assertEquals(14, viewModel.state.value.selectedWeekNumber)
        assertEquals(listOf("race-14"), viewModel.state.value.raceCards.map { it.raceId })

        viewModel.onAction(ScheduleAction.PreviousWeek)
        viewModel.onAction(ScheduleAction.Today)
        advanceUntilIdle()

        assertEquals(13, viewModel.state.value.selectedWeekNumber)
        assertEquals(1, repository.loadCount)
    }

    private fun viewModelFor(
        result: PlannerDataResult<PlannerData>,
        currentWeekNumber: () -> Int? = { null },
    ): ScheduleViewModel =
        viewModelFor(
            repository = FakePlannerDataRepository { result },
            currentWeekNumber = currentWeekNumber,
        )

    private fun viewModelFor(
        repository: FakePlannerDataRepository,
        currentWeekNumber: () -> Int? = { null },
    ): ScheduleViewModel =
        ScheduleViewModel(
            loadPlannerData = LoadPlannerDataUseCase(repository),
            currentWeekNumber = currentWeekNumber,
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
        raceWeeks: List<RaceWeek> = listOf(sampleRaceWeek(number = 13)),
        plannerRaces: List<PlannerRace> = listOf(samplePlannerRace(id = "race-13", weekNumber = 13)),
        cars: List<PlannerCar> = sampleCars(),
        tracks: List<PlannerTrack> = sampleTracks(),
    ): PlannerData =
        PlannerData(
            raceWeeks = raceWeeks,
            plannerRaces = plannerRaces,
            cars = cars,
            tracks = tracks,
        )

    private fun sampleRaceWeek(number: Int): RaceWeek {
        val offset = ((number - 12) * 7 * 24 * 60).minutes
        return RaceWeek(
            number = RaceWeekNumber(number),
            window = TimeWindow(
                startsAt = Instant.parse("2026-06-09T00:00:00Z") + offset,
                endsAt = Instant.parse("2026-06-16T00:00:00Z") + offset,
            ),
        )
    }

    private fun samplePlannerRace(
        id: String,
        weekNumber: Int,
    ): PlannerRace =
        PlannerRace(
            id = RaceId(id),
            seriesId = SeriesId("series-global-mazda-mx5"),
            weekNumber = RaceWeekNumber(weekNumber),
            window = sampleRaceWeek(weekNumber).window,
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

    private fun sampleCars(): List<PlannerCar> =
        listOf(
            PlannerCar(
                id = CarId("mazda-mx5-cup"),
                displayName = "Global Mazda MX-5 Cup",
                sourceCarId = 35,
            ),
        )

    private fun sampleTracks(): List<PlannerTrack> =
        listOf(
            PlannerTrack(
                id = TrackId("okayama-international-circuit"),
                displayName = "Okayama International Circuit",
                sourceTrackIds = setOf(123),
                primaryType = TrackType.ROAD,
            ),
        )
}
