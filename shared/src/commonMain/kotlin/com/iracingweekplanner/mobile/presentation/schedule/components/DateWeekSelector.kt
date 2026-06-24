package com.iracingweekplanner.mobile.presentation.schedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.presentation.schedule.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.schedule.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.schedule.preview.ScheduleUiPreviewData
import com.iracingweekplanner.mobile.presentation.theme.IwpAppTheme

@Composable
fun DateWeekSelector(
    content: DateWeekSelectorContent,
    onPreviousClick: () -> Unit,
    onTodayClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ScheduleUiTokens.ControlRadius),
        tonalElevation = ScheduleUiTokens.RaceCardBorderWidth,
    ) {
        Row(
            modifier = Modifier.padding(ScheduleUiTokens.DefaultGap),
            horizontalArrangement = Arrangement.spacedBy(ScheduleUiTokens.DefaultGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onPreviousClick,
                enabled = content.previousEnabled,
                modifier = Modifier.sizeIn(
                    minWidth = ScheduleUiTokens.MinimumIconTouchTarget,
                    minHeight = ScheduleUiTokens.MinimumIconTouchTarget,
                ),
            ) {
                Text("Prev")
            }
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
            TextButton(
                onClick = onTodayClick,
                modifier = Modifier.sizeIn(
                    minWidth = ScheduleUiTokens.MinimumIconTouchTarget,
                    minHeight = ScheduleUiTokens.MinimumIconTouchTarget,
                ),
            ) {
                Text("Today")
            }
            TextButton(
                onClick = onNextClick,
                enabled = content.nextEnabled,
                modifier = Modifier.sizeIn(
                    minWidth = ScheduleUiTokens.MinimumIconTouchTarget,
                    minHeight = ScheduleUiTokens.MinimumIconTouchTarget,
                ),
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
@Preview
private fun DateWeekSelectorPreview() {
    IwpAppTheme {
        DateWeekSelector(
            content = ScheduleUiPreviewData.foundationSample().selector,
            onPreviousClick = {},
            onTodayClick = {},
            onNextClick = {},
            modifier = Modifier.padding(ScheduleUiTokens.ScreenPaddingHorizontal),
        )
    }
}
