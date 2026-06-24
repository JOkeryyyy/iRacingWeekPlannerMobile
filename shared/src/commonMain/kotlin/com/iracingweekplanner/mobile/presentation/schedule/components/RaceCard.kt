package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleRaceCardContent
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData

@Composable
fun RaceCard(
    content: ScheduleRaceCardContent,
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
            Text(
                text = content.carSummary,
                fontSize = ScheduleUiTokens.CaptionTextSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (content.metadata.isNotEmpty()) {
                Text(
                    text = content.metadata.joinToString(separator = " | "),
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
@ScheduleComponentPreview
private fun RaceCardPreview() {
    ScheduleComponentPreviewTheme {
        RaceCard(
            content = ScheduleUiPreviewData.foundationSample().raceCard,
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}
