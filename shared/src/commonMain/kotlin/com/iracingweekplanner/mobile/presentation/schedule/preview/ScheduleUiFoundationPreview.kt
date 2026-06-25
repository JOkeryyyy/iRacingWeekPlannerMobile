package com.iracingweekplanner.mobile.presentation.schedule.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iracingweekplanner.mobile.presentation.common.components.DateWeekSelector
import com.iracingweekplanner.mobile.presentation.common.components.RaceCard
import com.iracingweekplanner.mobile.presentation.common.components.ScheduleBottomNavigation
import com.iracingweekplanner.mobile.presentation.common.components.ScheduleChip
import com.iracingweekplanner.mobile.presentation.common.components.ScheduleHeader
import com.iracingweekplanner.mobile.presentation.common.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.common.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.common.preview.ScheduleUiPreviewData
import com.iracingweekplanner.mobile.presentation.common.theme.IwpAppTheme

@Composable
@IWPPreview
fun ScheduleUiFoundationPreview() {
    val sample = ScheduleUiPreviewData.foundationResourceSample()

    IwpAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = ScheduleUiTokens.ScreenPaddingHorizontal,
                        top = ScheduleUiTokens.ScreenPaddingTop,
                        end = ScheduleUiTokens.ScreenPaddingHorizontal,
                        bottom = ScheduleUiTokens.ScreenPaddingBottom,
                    ),
                verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.SectionGap),
            ) {
                ScheduleHeader(content = sample.header, onRefreshClick = {})
                DateWeekSelector(
                    content = sample.selector,
                    onPreviousClick = {},
                    onTodayClick = {},
                    onNextClick = {},
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
                    verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
                ) {
                    sample.chips.forEach { chip ->
                        ScheduleChip(content = chip)
                    }
                }
                RaceCard(content = sample.raceCard)
                Spacer(modifier = Modifier.weight(1f))
                ScheduleBottomNavigation(tabs = sample.bottomTabs, onTabClick = {})
            }
        }
    }
}
