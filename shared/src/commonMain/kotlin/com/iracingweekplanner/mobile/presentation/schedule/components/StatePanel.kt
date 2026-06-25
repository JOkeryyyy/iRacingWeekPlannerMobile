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
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelContent
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleStatePanelVariant
import com.iracingweekplanner.mobile.presentation.schedule.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData

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
                    icon = ScheduleButtonIcons.Retry,
                    contentDescription = retryLabel,
                    onClick = onRetryClick,
                    emphasis = ScheduleButtonEmphasis.Filled,
                )
            }
        }
    }
}

@Composable
@IWPPreview
private fun StatePanelLoadingPreview() {
    ScheduleComponentPreviewTheme {
        StatePanel(
            content = ScheduleUiPreviewData.loadingPanelResourceSample(),
            onRetryClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}

@Composable
@IWPPreview
private fun StatePanelEmptyPreview() {
    ScheduleComponentPreviewTheme {
        StatePanel(
            content = ScheduleUiPreviewData.emptyPanelResourceSample(),
            onRetryClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}

@Composable
@IWPPreview
private fun StatePanelPreview() {
    ScheduleComponentPreviewTheme {
        StatePanel(
            content = ScheduleUiPreviewData.errorPanelResourceSample(),
            onRetryClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}
