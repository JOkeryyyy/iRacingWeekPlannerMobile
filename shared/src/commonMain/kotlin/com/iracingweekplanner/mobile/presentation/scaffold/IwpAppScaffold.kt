package com.iracingweekplanner.mobile.presentation.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iracingweekplanner.mobile.presentation.schedule.components.ScheduleBottomNavigation
import com.iracingweekplanner.mobile.presentation.schedule.components.ScheduleHeader
import com.iracingweekplanner.mobile.presentation.schedule.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData

@Composable
fun IwpAppScaffold(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)),
        ) {
            topBar()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
            ) {
                content(contentPadding)
            }
            bottomBar()
        }
    }
}

@Composable
@IWPPreview
private fun IwpAppScaffoldPreview() {
    ScheduleComponentPreviewTheme {
        val sample = ScheduleUiPreviewData.foundationResourceSample()

        IwpAppScaffold(
            contentPadding = PaddingValues(16.dp),
            topBar = {
                ScheduleHeader(
                    content = sample.header,
                    onRefreshClick = {},
                    modifier = Modifier.padding(16.dp),
                )
            },
            bottomBar = {
                ScheduleBottomNavigation(
                    tabs = sample.bottomTabs,
                    onTabClick = {},
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Scaffold content")
            }
        }
    }
}
