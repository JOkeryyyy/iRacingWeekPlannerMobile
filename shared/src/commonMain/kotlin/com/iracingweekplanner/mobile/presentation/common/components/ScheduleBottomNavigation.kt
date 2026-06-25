package com.iracingweekplanner.mobile.presentation.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iracingweekplanner.mobile.presentation.common.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.common.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.common.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.common.preview.ScheduleUiPreviewData
import org.jetbrains.compose.resources.painterResource

@Composable
fun ScheduleBottomNavigation(
    tabs: List<ScheduleBottomTab>,
    onTabClick: (ScheduleBottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = ScheduleUiTokens.BottomNavigationMinHeight,
                    max = ScheduleUiTokens.BottomNavigationMaxHeight,
                ),
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
        ) {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = tab.selected,
                    enabled = tab.enabled,
                    onClick = { onTabClick(tab) },
                    icon = {
                        Icon(
                            painter = painterResource(tab.icon),
                            contentDescription = tab.label,
                        )
                    },
                    label = { Text(tab.label) },
                )
            }
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
