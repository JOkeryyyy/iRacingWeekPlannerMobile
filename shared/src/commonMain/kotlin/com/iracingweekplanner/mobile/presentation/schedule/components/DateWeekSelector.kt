package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.schedule.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleComponentPreviewTheme
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData

@Composable
fun DateWeekSelector(
    content: DateWeekSelectorContent,
    onPreviousClick: () -> Unit,
    onTodayClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScheduleCard(
        modifier = modifier,
        shape = RoundedCornerShape(ScheduleUiTokens.ControlRadius),
        border = null,
        elevation = ScheduleUiTokens.RaceCardBorderWidth,
        contentPadding = PaddingValues(ScheduleUiTokens.DefaultGap),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ScheduleUiTokens.DefaultGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScheduleButton(
                icon = ScheduleButtonIcons.Previous,
                contentDescription = content.previousContentDescription,
                onClick = onPreviousClick,
                enabled = content.previousEnabled,
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = content.weekLabel,
                    fontSize = ScheduleUiTokens.SectionTitleTextSize,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = content.dateContext,
                    fontSize = ScheduleUiTokens.CaptionTextSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ScheduleButton(
                icon = ScheduleButtonIcons.Today,
                contentDescription = content.todayLabel,
                onClick = onTodayClick,
            )
            ScheduleButton(
                icon = ScheduleButtonIcons.Next,
                contentDescription = content.nextContentDescription,
                onClick = onNextClick,
                enabled = content.nextEnabled,
            )
        }
    }
}

@Composable
@IWPPreview
private fun DateWeekSelectorPreview() {
    ScheduleComponentPreviewTheme {
        DateWeekSelector(
            content = ScheduleUiPreviewData.foundationResourceSample().selector,
            onPreviousClick = {},
            onTodayClick = {},
            onNextClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}
