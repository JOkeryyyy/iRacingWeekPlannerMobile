package com.iracingweekplanner.mobile.presentation.common.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.common.theme.IwpAppTheme

private const val UI_MODE_NIGHT_YES = 0x20

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Large font", fontScale = 1.8f)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class IWPPreview

@Composable
fun ScheduleComponentPreviewTheme(
    content: @Composable () -> Unit,
) {
    IwpAppTheme {
        content()
    }
}
