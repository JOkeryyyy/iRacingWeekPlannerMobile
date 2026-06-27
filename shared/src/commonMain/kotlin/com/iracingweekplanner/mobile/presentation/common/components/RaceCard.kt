package com.iracingweekplanner.mobile.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.iracingweekplanner.mobile.presentation.common.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardUi
import com.iracingweekplanner.mobile.presentation.common.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.common.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.common.preview.ScheduleUiPreviewData

@Composable
fun RaceCard(
    content: ScheduleRaceCardUi,
    modifier: Modifier = Modifier,
) {
    ScheduleCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.RaceCardInternalGap),
        ) {
            Text(
                text = content.title,
                fontSize = ScheduleUiTokens.RaceTitleTextSize,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = content.track,
                fontSize = ScheduleUiTokens.CaptionTextSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            content.carSummary?.takeIf { it.isNotBlank() }?.let { carSummary ->
                Text(
                    text = carSummary,
                    fontSize = ScheduleUiTokens.CaptionTextSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content.metadataText?.takeIf { it.isNotBlank() }?.let { metadataText ->
                Text(
                    text = metadataText,
                    fontSize = ScheduleUiTokens.MetadataTextSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (content.chips.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
                    verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
                ) {
                    content.chips.forEach { chip ->
                        ScheduleChip(content = chip)
                    }
                }
            }
        }
    }
}

@Composable
@IWPPreview
private fun RaceCardPreview() {
    ScheduleComponentPreviewTheme {
        RaceCard(
            content = ScheduleUiPreviewData.foundationResourceSample().raceCard,
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}
