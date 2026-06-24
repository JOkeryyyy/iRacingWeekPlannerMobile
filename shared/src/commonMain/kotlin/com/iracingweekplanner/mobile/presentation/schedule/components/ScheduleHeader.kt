package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData
import com.iracingweekplanner.mobile.presentation.theme.IwpAppTheme

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
        TextButton(
            onClick = onRefreshClick,
            modifier = Modifier
                .sizeIn(
                    minWidth = ScheduleUiTokens.MinimumIconTouchTarget,
                    minHeight = ScheduleUiTokens.MinimumIconTouchTarget,
                )
                .semantics {
                    role = Role.Button
                    contentDescription = "Refresh schedule"
                },
        ) {
            Text("Refresh")
        }
    }
}

@Composable
@Preview
private fun ScheduleHeaderPreview() {
    IwpAppTheme {
        ScheduleHeader(
            content = ScheduleUiPreviewData.foundationSample().header,
            onRefreshClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}
