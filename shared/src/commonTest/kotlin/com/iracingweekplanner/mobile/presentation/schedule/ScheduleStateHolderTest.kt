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
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelVariant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleStateHolderTest {

    @Test
    fun loadedPlannerDataDerivesSelectedWeekScheduleState() = runTest {
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
        val stateHolder = ScheduleStateHolder(
            plannerData = plannerData,
            scope = backgroundScope,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        val state = stateHolder.state.value

        assertEquals(13, state.selectedWeekNumber)
        assertEquals("Week 13 Schedule", state.scheduleTitle)
        assertNull(state.lastUpdatedDisplayText)
        assertEquals("1 race", state.raceCountText)
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertFalse(state.isCached)
        assertNull(state.statePanel)
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
        val stateHolder = ScheduleStateHolder(
            plannerData = plannerData,
            scope = backgroundScope,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        val state = stateHolder.state.value

        assertTrue(state.isCached)
        assertEquals("1 race", state.raceCountText)
        assertEquals(listOf("race-13"), state.raceCards.map { it.raceId })
        assertEquals(ScheduleStatePanelVariant.Empty, assertNotNull(state.statePanel).variant)
    }

    @Test
    fun emptyPlannerDataRequestsEmptyPanelInsteadOfBlankRaceList() = runTest {
        val plannerData = FakePlannerDataPresenter(
            PlannerDataUiState.Empty(
                freshness = PlannerDataFreshness.FRESH,
                message = null,
            ),
        )
        val stateHolder = ScheduleStateHolder(
            plannerData = plannerData,
            scope = backgroundScope,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        val state = stateHolder.state.value

        assertTrue(state.isEmpty)
        assertEquals("0 races", state.raceCountText)
        assertTrue(state.raceCards.isEmpty())
        assertEquals(ScheduleStatePanelVariant.Empty, assertNotNull(state.statePanel).variant)
    }

    @Test
    fun sourceErrorWithoutCacheRequestsRetryableErrorPanel() = runTest {
        val plannerData = FakePlannerDataPresenter(
            PlannerDataUiState.Error(PlannerDataUiMessage.PLANNER_DATA_UNAVAILABLE),
        )
        val stateHolder = ScheduleStateHolder(
            plannerData = plannerData,
            scope = backgroundScope,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        val panel = assertNotNull(stateHolder.state.value.statePanel)

        assertEquals(ScheduleStatePanelVariant.Error, panel.variant)
        assertTrue(panel.canRetry)
        assertTrue(stateHolder.state.value.raceCards.isEmpty())
    }

    @Test
    fun initialLoadRunsOnlyOnePlannerLoadAction() = runTest {
        val plannerData = FakePlannerDataPresenter(PlannerDataUiState.Idle)
        val stateHolder = ScheduleStateHolder(
            plannerData = plannerData,
            scope = backgroundScope,
            currentWeekNumber = { 13 },
        )

        stateHolder.onAction(ScheduleAction.InitialLoad)
        stateHolder.onAction(ScheduleAction.InitialLoad)

        assertEquals(listOf<PlannerDataAction>(PlannerDataAction.Load), plannerData.actions)
    }

    @Test
    fun refreshAndRetryEachForwardOnePlannerRetryAction() = runTest {
        val plannerData = FakePlannerDataPresenter(PlannerDataUiState.Idle)
        val stateHolder = ScheduleStateHolder(
            plannerData = plannerData,
            scope = backgroundScope,
            currentWeekNumber = { 13 },
        )

        stateHolder.onAction(ScheduleAction.Refresh)
        stateHolder.onAction(ScheduleAction.Retry)

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
        val stateHolder = ScheduleStateHolder(
            plannerData = plannerData,
            scope = backgroundScope,
            currentWeekNumber = { 13 },
        )
        advanceUntilIdle()

        stateHolder.onAction(ScheduleAction.NextWeek)
        advanceUntilIdle()
        assertEquals(14, stateHolder.state.value.selectedWeekNumber)
        assertEquals(listOf("race-14"), stateHolder.state.value.raceCards.map { it.raceId })

        stateHolder.onAction(ScheduleAction.PreviousWeek)
        stateHolder.onAction(ScheduleAction.Today)
        advanceUntilIdle()

        assertEquals(13, stateHolder.state.value.selectedWeekNumber)
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
