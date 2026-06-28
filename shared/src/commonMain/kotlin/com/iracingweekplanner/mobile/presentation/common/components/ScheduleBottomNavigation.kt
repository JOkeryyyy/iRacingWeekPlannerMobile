package com.iracingweekplanner.mobile.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(
                    min = ScheduleUiTokens.BottomNavigationMinHeight,
                )
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = ScheduleUiTokens.CompactGap),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                ScheduleBottomNavigationItem(
                    tab = tab,
                    onClick = { onTabClick(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ScheduleBottomNavigationItem(
    tab: ScheduleBottomTab,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val enabledContentColor = if (tab.selected) {
        colorScheme.primary
    } else {
        colorScheme.onSurfaceVariant
    }
    val contentColor = if (tab.enabled) {
        enabledContentColor
    } else {
        colorScheme.onSurface.copy(alpha = ScheduleBottomNavigationDefaults.DisabledAlpha)
    }

    Column(
        modifier = modifier
            .heightIn(min = ScheduleUiTokens.BottomNavigationItemMinTouchHeight)
            .selectable(
                selected = tab.selected,
                enabled = tab.enabled,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(
                horizontal = ScheduleUiTokens.CompactGap,
                vertical = ScheduleUiTokens.CompactGap,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = ScheduleUiTokens.BottomNavigationSelectedIconContainerWidth,
                    height = ScheduleUiTokens.BottomNavigationSelectedIconContainerHeight,
                )
                .background(
                    color = if (tab.selected) {
                        colorScheme.primaryContainer
                    } else {
                        colorScheme.background
                    },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(tab.icon),
                contentDescription = null,
                tint = if (tab.selected && tab.enabled) {
                    colorScheme.onPrimaryContainer
                } else {
                    contentColor
                },
            )
        }
        Text(
            text = tab.label,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
        )
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

private object ScheduleBottomNavigationDefaults {
    const val DisabledAlpha = 0.38f
}
