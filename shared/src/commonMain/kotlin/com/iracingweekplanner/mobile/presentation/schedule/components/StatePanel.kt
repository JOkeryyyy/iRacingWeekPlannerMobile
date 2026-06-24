package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelVariant
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData
import com.iracingweekplanner.mobile.presentation.theme.IwpAppTheme

@Composable
fun StatePanel(
    content: ScheduleStatePanelContent,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScheduleCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        contentPadding = PaddingValues(ScheduleUiTokens.SectionGap),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.DefaultGap),
            horizontalAlignment = Alignment.Start,
        ) {
            if (content.variant == ScheduleStatePanelVariant.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.sizeIn(
                        minWidth = ScheduleUiTokens.MinimumChipHeight,
                        minHeight = ScheduleUiTokens.MinimumChipHeight,
                    ),
                )
            }
            Text(
                text = content.title,
                fontSize = ScheduleUiTokens.SectionTitleTextSize,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = content.message,
                fontSize = ScheduleUiTokens.CaptionTextSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content.retryLabel?.let { retryLabel ->
                ScheduleButton(
                    label = retryLabel,
                    onClick = onRetryClick,
                    style = ScheduleButtonStyle.Filled,
                )
            }
        }
    }
}

@Composable
@Preview
private fun StatePanelLoadingPreview() {
    IwpAppTheme {
        StatePanel(
            content = ScheduleUiPreviewData.loadingPanelSample(),
            onRetryClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}

@Composable
@Preview
private fun StatePanelEmptyPreview() {
    IwpAppTheme {
        StatePanel(
            content = ScheduleUiPreviewData.emptyPanelSample(),
            onRetryClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}

@Composable
@Preview
private fun StatePanelPreview() {
    IwpAppTheme {
        StatePanel(
            content = ScheduleUiPreviewData.errorPanelSample(),
            onRetryClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}
