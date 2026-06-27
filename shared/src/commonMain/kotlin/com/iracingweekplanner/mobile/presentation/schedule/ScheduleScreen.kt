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
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleRaceCardUi
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelContent
import com.iracingweekplanner.mobile.presentation.common.preview.IWPPreview
import com.iracingweekplanner.mobile.presentation.common.theme.IwpAppTheme

@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    onAction: (ScheduleAction) -> Unit,
    onTabClick: (ScheduleBottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomTabs = ScheduleTextResources.bottomTabs()

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
                tabs = bottomTabs,
                onTabClick = onTabClick,
            )
        },
    ) { contentPadding ->
        ScheduleScreenBody(
            state = state,
            onAction = onAction,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
private fun ScheduleScreenBody(
    state: ScheduleUiState,
    onAction: (ScheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.SectionGap),
    ) {
        ScheduleHeader(
            content = ScheduleTextResources.headerContent(
                weekNumber = state.selectedWeekNumber,
                lastUpdatedTime = state.lastUpdatedDisplayText,
            ),
            onRefreshClick = { onAction(ScheduleAction.Refresh) },
        )
        DateWeekSelector(
            content = ScheduleTextResources.dateWeekSelectorContent(
                weekNumber = state.selectedWeekNumber,
                dateContext = ScheduleTextResources.loadingDateContext(),
                previousEnabled = state.canSelectPreviousWeek,
                nextEnabled = state.canSelectNextWeek,
            ),
            onPreviousClick = { onAction(ScheduleAction.PreviousWeek) },
            onTodayClick = { onAction(ScheduleAction.Today) },
            onNextClick = { onAction(ScheduleAction.NextWeek) },
        )
        ScheduleSummaryChips(
            selectedWeekNumber = state.selectedWeekNumber,
            raceCount = state.raceCards.size,
        )
        ScheduleRaceList(
            statePanel = state.toStatePanelContent(),
            raceCards = state.raceCards,
            onRetryClick = { onAction(ScheduleAction.Retry) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScheduleSummaryChips(
    selectedWeekNumber: Int,
    raceCount: Int,
    modifier: Modifier = Modifier,
) {
    val chips = listOf(
        ScheduleChipContent(
            label = ScheduleTextResources.weekLabel(selectedWeekNumber),
            selected = true,
        ),
        ScheduleChipContent(label = ScheduleTextResources.raceCount(raceCount)),
    )

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
    statePanel: ScheduleStatePanelContent?,
    raceCards: List<ScheduleRaceCardUi>,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ScheduleUiTokens.RaceCardListGap),
        contentPadding = PaddingValues(bottom = ScheduleUiTokens.DefaultGap),
    ) {
        statePanel?.let { panel ->
            item {
                StatePanel(
                    content = panel,
                    onRetryClick = onRetryClick,
                )
            }
        }
        items(raceCards) { raceCard ->
            RaceCard(content = ScheduleTextResources.raceCardContent(raceCard))
        }
    }
}

@Composable
private fun ScheduleUiState.toStatePanelContent() =
    when {
        isLoading -> ScheduleTextResources.loadingPanelContent()
        panelMessage != null -> ScheduleTextResources.statePanelContent(panelMessage)
        isEmpty -> ScheduleTextResources.emptyPanelContent()
        else -> null
    }

@Composable
@IWPPreview
private fun ScheduleScreenPreview() {
    IwpAppTheme {
        ScheduleScreen(
            state = ScheduleUiState(
                selectedWeekNumber = ScheduleScreenDefaults.SelectedWeekNumber,
                availableWeekNumbers = emptyList(),
                lastUpdatedDisplayText = null,
                raceCards = emptyList(),
                panelMessage = null,
                isLoading = true,
                isEmpty = false,
                isCached = false,
                canSelectPreviousWeek = false,
                canSelectNextWeek = false,
            ),
            onAction = {},
            onTabClick = {},
        )
    }
}

private object ScheduleScreenDefaults {
    const val SelectedWeekNumber = 13
}
