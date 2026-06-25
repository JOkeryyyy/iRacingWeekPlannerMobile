package com.iracingweekplanner.mobile.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleShell
import com.iracingweekplanner.mobile.presentation.theme.IwpAppTheme

@Composable
fun App() {
    IwpAppTheme {
        ScheduleShell()
    }
}

@Composable
@Preview
fun AppPreview() {
    App()
}
