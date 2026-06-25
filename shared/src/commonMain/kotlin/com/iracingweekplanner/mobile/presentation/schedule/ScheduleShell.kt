package com.iracingweekplanner.mobile.presentation.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iracingweekplanner.mobile.presentation.common.scaffold.IwpAppScaffold
import com.iracingweekplanner.mobile.presentation.common.components.DateWeekSelector
import com.iracingweekplanner.mobile.presentation.common.components.RaceCard
import com.iracingweekplanner.mobile.presentation.common.components.ScheduleBottomNavigation
import com.iracingweekplanner.mobile.presentation.common.components.ScheduleChip
import com.iracingweekplanner.mobile.presentation.common.components.ScheduleHeader
import com.iracingweekplanner.mobile.presentation.common.components.StatePanel
import com.iracingweekplanner.mobile.presentation.common.design.ScheduleUiTokens
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleChipContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleShellContent
import com.iracingweekplanner.mobile.presentation.common.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.common.theme.IwpAppTheme

@Composable
fun ScheduleShell(
    modifier: Modifier = Modifier,
    selectedWeekNumber: Int = ScheduleShellDefaults.SelectedWeekNumber,
) {
    ScheduleShell(
        content = ScheduleShellContent(
            selectedWeekNumber = selectedWeekNumber,
            header = ScheduleTextResources.headerContent(
                weekNumber = selectedWeekNumber,
                lastUpdatedTime = null,
            ),
            selector = ScheduleTextResources.dateWeekSelectorContent(
                weekNumber = selectedWeekNumber,
                dateContext = ScheduleTextResources.loadingDateContext(),
                previousEnabled = false,
                nextEnabled = false,
            ),
            summaryChips = listOf(
                ScheduleChipContent(
                    label = ScheduleTextResources.weekLabel(selectedWeekNumber),
                    selected = true,
                ),
                ScheduleChipContent(label = ScheduleTextResources.raceCount(count = 0)),
            ),
            statePanel = ScheduleTextResources.loadingPanelContent(),
            bottomTabs = ScheduleTextResources.bottomTabs(),
        ),
        onRefreshClick = {},
        onPreviousWeekClick = {},
        onTodayClick = {},
        onNextWeekClick = {},
        onRetryClick = {},
        onTabClick = {},
        modifier = modifier,
    )
}

@Composable
fun ScheduleShell(
    content: ScheduleShellContent,
    onRefreshClick: () -> Unit,
    onPreviousWeekClick: () -> Unit,
    onTodayClick: () -> Unit,
    onNextWeekClick: () -> Unit,
    onRetryClick: () -> Unit,
    onTabClick: (ScheduleBottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    IwpAppScaffold(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = ScheduleUiTokens.ScreenPaddingHorizontal,
            top = ScheduleUiTokens.ScreenPaddingTop,
            end = ScheduleUiTokens.ScreenPaddingHorizontal,
            bottom = ScheduleUiTokens.ScreenPaddingBottom,
        ),
        bottomBar = {
            ScheduleBottomNavigation(
                tabs = content.bottomTabs,
                onTabClick = onTabClick,
            )
        },
    ) { contentPadding ->
        ScheduleShellBody(
            content = content,
            onRefreshClick = onRefreshClick,
            onPreviousWeekClick = onPreviousWeekClick,
            onTodayClick = onTodayClick,
            onNextWeekClick = onNextWeekClick,
            onRetryClick = onRetryClick,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
private fun ScheduleShellBody(
    content: ScheduleShellContent,
    onRefreshClick: () -> Unit,
    onPreviousWeekClick: () -> Unit,
    onTodayClick: () -> Unit,
    onNextWeekClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.SectionGap),
    ) {
        ScheduleHeader(
            content = content.header,
            onRefreshClick = onRefreshClick,
        )
        DateWeekSelector(
            content = content.selector,
            onPreviousClick = onPreviousWeekClick,
            onTodayClick = onTodayClick,
            onNextClick = onNextWeekClick,
        )
        ScheduleSummaryChips(chips = content.summaryChips)
        ScheduleRaceList(
            content = content,
            onRetryClick = onRetryClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScheduleSummaryChips(
    chips: List<ScheduleChipContent>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
        verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.CompactGap),
    ) {
        chips.forEach { chip ->
            ScheduleChip(content = chip)
        }
    }
}

@Composable
private fun ScheduleRaceList(
    content: ScheduleShellContent,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.RaceCardListGap),
        contentPadding = PaddingValues(bottom = ScheduleUiTokens.DefaultGap),
    ) {
        content.statePanel?.let { panel ->
            item {
                StatePanel(
                    content = panel,
                    onRetryClick = onRetryClick,
                )
            }
        }
        items(content.raceCards) { raceCard ->
            RaceCard(content = raceCard)
        }
    }
}

@Composable
@IWPPreview
private fun ScheduleShellPreview() {
    IwpAppTheme {
        ScheduleShell()
    }
}

private object ScheduleShellDefaults {
    const val SelectedWeekNumber = 13
}
