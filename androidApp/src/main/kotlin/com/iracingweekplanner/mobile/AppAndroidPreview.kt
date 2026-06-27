package com.iracingweekplanner.mobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.common.theme.IwpAppTheme
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleAction
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleScreen
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleUiState

@Preview
@Composable
fun AppAndroidPreview() {
    IwpAppTheme {
        ScheduleScreen(
            state = ScheduleUiState(
                selectedWeekNumber = 13,
                availableWeekNumbers = emptyList(),
                lastUpdatedDisplayText = null,
                raceCards = emptyList(),
                panelMessage = null,
                isLoading = true,
                isEmpty = false,
                isCached = false,
                canSelectPreviousWeek = false,
                canSelectNextWeek = false,
            ),
            onAction = { _: ScheduleAction -> },
            onTabClick = {},
        )
    }
}
