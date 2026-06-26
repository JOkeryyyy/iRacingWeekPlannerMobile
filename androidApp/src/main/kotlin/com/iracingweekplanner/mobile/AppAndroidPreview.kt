package com.iracingweekplanner.mobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.common.theme.IwpAppTheme
import com.iracingweekplanner.mobile.presentation.schedule.ScheduleShell

@Preview
@Composable
fun AppAndroidPreview() {
    IwpAppTheme {
        ScheduleShell()
    }
}
