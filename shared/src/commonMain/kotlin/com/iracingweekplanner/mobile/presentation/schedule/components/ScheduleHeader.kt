package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.schedule.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData

@Composable
fun ScheduleHeader(
    content: ScheduleHeaderContent,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ScheduleUiTokens.DefaultGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
        ) {
            Text(
                text = content.weekTitle,
                fontSize = ScheduleUiTokens.ScheduleTitleTextSize,
                fontWeight = FontWeight.SemiBold,
            )
            content.lastUpdatedText?.let { lastUpdatedText ->
                Text(
                    text = lastUpdatedText,
                    fontSize = ScheduleUiTokens.CaptionTextSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        ScheduleButton(
            icon = ScheduleButtonIcons.Refresh,
            contentDescription = content.refreshContentDescription,
            onClick = onRefreshClick,
            emphasis = ScheduleButtonEmphasis.Filled,
        )
    }
}

@Composable
@IWPPreview
private fun ScheduleHeaderPreview() {
    ScheduleComponentPreviewTheme {
        ScheduleHeader(
            content = ScheduleUiPreviewData.foundationResourceSample().header,
            onRefreshClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}
