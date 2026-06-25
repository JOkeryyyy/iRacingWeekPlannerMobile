package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData

@Composable
fun ScheduleChip(
    content: ScheduleChipContent,
    modifier: Modifier = Modifier,
) {
    val containerColor =
        if (content.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor =
        if (content.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.heightIn(min = ScheduleUiTokens.MinimumChipHeight),
        shape = RoundedCornerShape(percent = 50),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(
            width = ScheduleUiTokens.RaceCardBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Text(
            text = content.label,
            modifier = Modifier.padding(
                horizontal = ScheduleUiTokens.DefaultGap,
                vertical = ScheduleUiTokens.CompactGap,
            ),
            fontSize = ScheduleUiTokens.ChipTextSize,
            fontWeight = if (content.selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
@ScheduleComponentPreview
private fun ScheduleChipPreview() {
    ScheduleComponentPreviewTheme {
        Row(
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
            horizontalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
        ) {
            ScheduleUiPreviewData.foundationResourceSample().chips.forEach { chip ->
                ScheduleChip(content = chip)
            }
        }
    }
}
