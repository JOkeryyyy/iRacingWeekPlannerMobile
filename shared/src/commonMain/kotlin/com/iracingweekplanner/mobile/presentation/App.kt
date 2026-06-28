package com.iracingweekplanner.mobile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iracingweekplanner.mobile.domain.usecase.LoadPlannerDataUseCase
import com.iracingweekplanner.mobile.presentation.common.theme.IwpAppTheme
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleAction
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleScreen
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleUiState
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleViewModel

@Composable
fun App(loadPlannerData: LoadPlannerDataUseCase) {
    IwpAppTheme {
        val viewModel = viewModel {
            ScheduleViewModel(loadPlannerData = loadPlannerData)
        }
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(viewModel) {
            viewModel.onAction(ScheduleAction.InitialLoad)
        }

        ScheduleScreen(
            state = state,
            onAction = viewModel::onAction,
            onTabClick = {},
        )
    }
}

@Composable
@Preview
fun AppPreview() {
    IwpAppTheme {
        ScheduleScreen(
            state = ScheduleUiState(
                selectedWeekNumber = 13,
                availableWeekNumbers = emptyList(),
                dateContext = null,
                lastUpdatedDisplayText = null,
                raceCards = emptyList(),
                panelMessage = null,
                isLoading = true,
                isEmpty = false,
                isCached = false,
                canSelectPreviousWeek = false,
                canSelectCurrentWeek = false,
                canSelectNextWeek = false,
            ),
            onAction = { _: ScheduleAction -> },
            onTabClick = {},
        )
    }
}
