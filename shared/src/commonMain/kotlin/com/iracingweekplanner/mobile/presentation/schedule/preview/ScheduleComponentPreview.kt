package com.iracingweekplanner.mobile.presentation.schedule.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.theme.IwpAppTheme

private const val UI_MODE_NIGHT_YES = 0x20

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Large font", fontScale = 1.5f)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class ScheduleComponentPreview

@Composable
fun ScheduleComponentPreviewTheme(
    content: @Composable () -> Unit,
) {
    IwpAppTheme {
        content()
    }
}
