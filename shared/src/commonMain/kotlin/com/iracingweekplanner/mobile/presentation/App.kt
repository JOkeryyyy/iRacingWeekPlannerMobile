package com.iracingweekplanner.mobile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleRoot
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleShell
import com.iracingweekplanner.mobile.presentation.common.theme.IwpAppTheme

@Composable
fun App(plannerData: PlannerDataPresenter) {
    IwpAppTheme {
        ScheduleRoot(plannerData = plannerData)
    }
}

@Composable
@Preview
fun AppPreview() {
    IwpAppTheme {
        ScheduleShell()
    }
}
