package com.iracingweekplanner.mobile.presentation.schedule

import com.iracingweekplanner.mobile.domain.model.CarId
import com.iracingweekplanner.mobile.domain.model.PlannerCar
import com.iracingweekplanner.mobile.domain.model.PlannerDataFreshness
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
import com.iracingweekplanner.mobile.presentation.PlannerDataAction
import com.iracingweekplanner.mobile.presentation.PlannerDataPresenter
import com.iracingweekplanner.mobile.presentation.PlannerDataUiMessage
import com.iracingweekplanner.mobile.presentation.PlannerDataUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    fun loadedPlannerDataDerivesSelectedWeekScheduleUiState() = runTest {
        val plannerData = FakePlannerDataPresenter(
            PlannerDataUiState.Loaded(
                raceWeeks = listOf(sampleRaceWeek(12), sampleRaceWeek(13)),
                plannerRaces = listOf(
                    samplePlannerRace(id = "race-12", weekNumber = 12),
                    samplePlannerRace(id = "race-13", weekNumber = 13),
                ),
                cars = sampleCars(),
                tracks = sampleTracks(),
                freshness = PlannerDataFreshness.FRESH,
            ),
        )
        val viewModel = ScheduleViewModel(
            plannerData = plannerData,
            currentWeekNumber = { 13 },
        )
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
        val plannerData = FakePlannerDataPresenter(
            PlannerDataUiState.Loaded(
                raceWeeks = listOf(sampleRaceWeek(13)),
                plannerRaces = listOf(samplePlannerRace(id = "race-13", weekNumber = 13)),
                cars = sampleCars(),
                tracks = sampleTracks(),
                freshness = PlannerDataFreshness.CACHED,
                message = PlannerDataUiMessage.SHOWING_CACHED_PLANNER_DATA,
            ),
        )
        val viewModel = ScheduleViewModel(
            plannerData = plannerData,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        val state = viewModel.state.value

        assertTrue(state.isCached)
        assertEquals(listOf("race-13"), state.raceCards.map { it.raceId })
        assertEquals(PlannerDataUiMessage.SHOWING_CACHED_PLANNER_DATA, state.panelMessage)
    }

    @Test
    fun emptyPlannerDataRequestsEmptyPanelInsteadOfBlankRaceList() = runTest {
        val plannerData = FakePlannerDataPresenter(
            PlannerDataUiState.Empty(
                freshness = PlannerDataFreshness.FRESH,
                message = null,
            ),
        )
        val viewModel = ScheduleViewModel(
            plannerData = plannerData,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        val state = viewModel.state.value

        assertTrue(state.isEmpty)
        assertTrue(state.raceCards.isEmpty())
        assertNull(state.panelMessage)
    }

    @Test
    fun sourceErrorWithoutCacheRequestsRetryableErrorPanel() = runTest {
        val plannerData = FakePlannerDataPresenter(
            PlannerDataUiState.Error(PlannerDataUiMessage.PLANNER_DATA_UNAVAILABLE),
        )
        val viewModel = ScheduleViewModel(
            plannerData = plannerData,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        val state = viewModel.state.value

        assertEquals(PlannerDataUiMessage.PLANNER_DATA_UNAVAILABLE, state.panelMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertTrue(state.raceCards.isEmpty())
    }

    @Test
    fun initialLoadRunsOnlyOnePlannerLoadAction() = runTest {
        val plannerData = FakePlannerDataPresenter(PlannerDataUiState.Idle)
        val viewModel = ScheduleViewModel(
            plannerData = plannerData,
            currentWeekNumber = { 13 },
        )

        viewModel.onAction(ScheduleAction.InitialLoad)
        viewModel.onAction(ScheduleAction.InitialLoad)
        advanceUntilIdle()

        assertEquals(listOf<PlannerDataAction>(PlannerDataAction.Load), plannerData.actions)
    }

    @Test
    fun refreshAndRetryEachForwardOnePlannerRetryAction() = runTest {
        val plannerData = FakePlannerDataPresenter(PlannerDataUiState.Idle)
        val viewModel = ScheduleViewModel(
            plannerData = plannerData,
            currentWeekNumber = { 13 },
        )

        viewModel.onAction(ScheduleAction.Refresh)
        viewModel.onAction(ScheduleAction.Retry)
        advanceUntilIdle()

        assertEquals(
            listOf<PlannerDataAction>(PlannerDataAction.Retry, PlannerDataAction.Retry),
            plannerData.actions,
        )
    }

    @Test
    fun weekSelectionActionsDoNotReloadPlannerData() = runTest {
        val plannerData = FakePlannerDataPresenter(
            PlannerDataUiState.Loaded(
                raceWeeks = listOf(sampleRaceWeek(12), sampleRaceWeek(13), sampleRaceWeek(14)),
                plannerRaces = listOf(
                    samplePlannerRace(id = "race-12", weekNumber = 12),
                    samplePlannerRace(id = "race-13", weekNumber = 13),
                    samplePlannerRace(id = "race-14", weekNumber = 14),
                ),
                cars = sampleCars(),
                tracks = sampleTracks(),
                freshness = PlannerDataFreshness.FRESH,
            ),
        )
        val viewModel = ScheduleViewModel(
            plannerData = plannerData,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        viewModel.onAction(ScheduleAction.NextWeek)
        advanceUntilIdle()
        assertEquals(14, viewModel.state.value.selectedWeekNumber)
        assertEquals(listOf("race-14"), viewModel.state.value.raceCards.map { it.raceId })

        viewModel.onAction(ScheduleAction.PreviousWeek)
        viewModel.onAction(ScheduleAction.Today)
        advanceUntilIdle()

        assertEquals(13, viewModel.state.value.selectedWeekNumber)
        assertTrue(plannerData.actions.isEmpty())
    }

    private class FakePlannerDataPresenter(
        initialState: PlannerDataUiState,
    ) : PlannerDataPresenter {
        private val mutableUiState = MutableStateFlow(initialState)
        override val uiState: StateFlow<PlannerDataUiState> = mutableUiState
        val actions = mutableListOf<PlannerDataAction>()

        override suspend fun onAction(action: PlannerDataAction) {
            actions += action
        }
    }

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
