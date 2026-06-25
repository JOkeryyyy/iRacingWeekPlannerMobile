package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.schedule.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData

@Composable
fun ScheduleBottomNavigation(
    tabs: List<ScheduleBottomTab>,
    onTabClick: (ScheduleBottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(
                min = ScheduleUiTokens.BottomNavigationMinHeight,
                max = ScheduleUiTokens.BottomNavigationMaxHeight,
            )
            .clip(
                RoundedCornerShape(
                    topStart = ScheduleUiTokens.BottomNavigationRadius,
                    topEnd = ScheduleUiTokens.BottomNavigationRadius,
                ),
            ),
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tab.selected,
                enabled = tab.enabled,
                onClick = { onTabClick(tab) },
                icon = { Text(tab.iconLabel) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
@IWPPreview
private fun ScheduleBottomNavigationPreview() {
    ScheduleComponentPreviewTheme {
        ScheduleBottomNavigation(
            tabs = ScheduleUiPreviewData.foundationResourceSample().bottomTabs,
            onTabClick = {},
        )
    }
}
