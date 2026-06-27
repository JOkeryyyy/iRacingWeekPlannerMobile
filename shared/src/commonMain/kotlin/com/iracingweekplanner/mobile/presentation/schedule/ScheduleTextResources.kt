package com.iracingweekplanner.mobile.presentation.schedule

import androidx.compose.runtime.Composable
import com.iracingweekplanner.mobile.presentation.common.model.DateWeekSelectorContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleBottomTab
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleHeaderContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelContent
import com.iracingweekplanner.mobile.presentation.common.model.ScheduleStatePanelVariant
import iracingweekplannermobile.shared.generated.resources.Res
import iracingweekplannermobile.shared.generated.resources.ic_favorites_tab
import iracingweekplannermobile.shared.generated.resources.ic_filters_tab
import iracingweekplannermobile.shared.generated.resources.ic_schedule_tab
import iracingweekplannermobile.shared.generated.resources.ic_settings_tab
import iracingweekplannermobile.shared.generated.resources.schedule_cached_data_message
import iracingweekplannermobile.shared.generated.resources.schedule_cached_message
import iracingweekplannermobile.shared.generated.resources.schedule_empty_message
import iracingweekplannermobile.shared.generated.resources.schedule_empty_title
import iracingweekplannermobile.shared.generated.resources.schedule_favorites_tab_label
import iracingweekplannermobile.shared.generated.resources.schedule_filters_tab_label
import iracingweekplannermobile.shared.generated.resources.schedule_invalid_data_message
import iracingweekplannermobile.shared.generated.resources.schedule_invalid_data_title
import iracingweekplannermobile.shared.generated.resources.schedule_last_updated
import iracingweekplannermobile.shared.generated.resources.schedule_loading_message
import iracingweekplannermobile.shared.generated.resources.schedule_loading_title
import iracingweekplannermobile.shared.generated.resources.schedule_local_store_error_message
import iracingweekplannermobile.shared.generated.resources.schedule_local_store_error_title
import iracingweekplannermobile.shared.generated.resources.schedule_next_week_content_description
import iracingweekplannermobile.shared.generated.resources.schedule_next_week_label
import iracingweekplannermobile.shared.generated.resources.schedule_previous_week_content_description
import iracingweekplannermobile.shared.generated.resources.schedule_previous_week_label
import iracingweekplannermobile.shared.generated.resources.schedule_race_count
import iracingweekplannermobile.shared.generated.resources.schedule_refresh_content_description
import iracingweekplannermobile.shared.generated.resources.schedule_refresh_label
import iracingweekplannermobile.shared.generated.resources.schedule_retry_label
import iracingweekplannermobile.shared.generated.resources.schedule_settings_tab_label
import iracingweekplannermobile.shared.generated.resources.schedule_source_error_message
import iracingweekplannermobile.shared.generated.resources.schedule_source_error_title
import iracingweekplannermobile.shared.generated.resources.schedule_tab_label
import iracingweekplannermobile.shared.generated.resources.schedule_today_label
import iracingweekplannermobile.shared.generated.resources.schedule_week_dates_loading
import iracingweekplannermobile.shared.generated.resources.schedule_week_label
import iracingweekplannermobile.shared.generated.resources.schedule_week_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

object ScheduleTextResources {
    fun bottomTabResources(): List<ScheduleBottomTabResources> =
        listOf(
            ScheduleBottomTabResources(
                label = Res.string.schedule_tab_label,
                icon = Res.drawable.ic_schedule_tab,
                selected = true,
                enabled = true,
            ),
            ScheduleBottomTabResources(
                label = Res.string.schedule_filters_tab_label,
                icon = Res.drawable.ic_filters_tab,
                selected = false,
                enabled = false,
            ),
            ScheduleBottomTabResources(
                label = Res.string.schedule_favorites_tab_label,
                icon = Res.drawable.ic_favorites_tab,
                selected = false,
                enabled = false,
            ),
            ScheduleBottomTabResources(
                label = Res.string.schedule_settings_tab_label,
                icon = Res.drawable.ic_settings_tab,
                selected = false,
                enabled = false,
            ),
        )

    fun loadingPanelResources(): ScheduleStatePanelResources =
        ScheduleStatePanelResources(
            variant = ScheduleStatePanelVariant.Loading,
            title = Res.string.schedule_loading_title,
            message = Res.string.schedule_loading_message,
        )

    fun emptyPanelResources(): ScheduleStatePanelResources =
        ScheduleStatePanelResources(
            variant = ScheduleStatePanelVariant.Empty,
            title = Res.string.schedule_empty_title,
            message = Res.string.schedule_empty_message,
        )

    fun statePanelResources(message: ScheduleUiMessage): ScheduleStatePanelResources =
        when (message) {
            ScheduleUiMessage.ShowingCachedPlannerData -> ScheduleStatePanelResources(
                variant = ScheduleStatePanelVariant.Empty,
                title = Res.string.schedule_cached_data_message,
                message = Res.string.schedule_cached_message,
            )
            ScheduleUiMessage.PlannerDataUnavailable -> ScheduleStatePanelResources(
                variant = ScheduleStatePanelVariant.Error,
                title = Res.string.schedule_source_error_title,
                message = Res.string.schedule_source_error_message,
                retryLabel = Res.string.schedule_retry_label,
            )
            ScheduleUiMessage.InvalidPlannerData -> ScheduleStatePanelResources(
                variant = ScheduleStatePanelVariant.Error,
                title = Res.string.schedule_invalid_data_title,
                message = Res.string.schedule_invalid_data_message,
                retryLabel = Res.string.schedule_retry_label,
            )
            ScheduleUiMessage.LocalPlannerDataUnavailable -> ScheduleStatePanelResources(
                variant = ScheduleStatePanelVariant.Error,
                title = Res.string.schedule_local_store_error_title,
                message = Res.string.schedule_local_store_error_message,
                retryLabel = Res.string.schedule_retry_label,
            )
        }

    @Composable
    fun headerContent(
        weekNumber: Int,
        lastUpdatedTime: String?,
    ): ScheduleHeaderContent =
        ScheduleHeaderContent(
            weekTitle = stringResource(Res.string.schedule_week_title, weekNumber),
            lastUpdatedText = lastUpdatedTime?.let { time ->
                stringResource(Res.string.schedule_last_updated, time)
            },
            refreshLabel = stringResource(Res.string.schedule_refresh_label),
            refreshContentDescription = stringResource(Res.string.schedule_refresh_content_description),
        )

    @Composable
    fun dateWeekSelectorContent(
        weekNumber: Int,
        dateContext: String,
        previousEnabled: Boolean,
        nextEnabled: Boolean,
    ): DateWeekSelectorContent =
        DateWeekSelectorContent(
            weekLabel = stringResource(Res.string.schedule_week_label, weekNumber),
            dateContext = dateContext,
            previousEnabled = previousEnabled,
            nextEnabled = nextEnabled,
            previousLabel = stringResource(Res.string.schedule_previous_week_label),
            previousContentDescription = stringResource(Res.string.schedule_previous_week_content_description),
            todayLabel = stringResource(Res.string.schedule_today_label),
            nextLabel = stringResource(Res.string.schedule_next_week_label),
            nextContentDescription = stringResource(Res.string.schedule_next_week_content_description),
        )

    @Composable
    fun bottomTabs(): List<ScheduleBottomTab> =
        bottomTabResources().map { tab ->
            ScheduleBottomTab(
                label = stringResource(tab.label),
                icon = tab.icon,
                selected = tab.selected,
                enabled = tab.enabled,
            )
        }

    @Composable
    fun loadingPanelContent(): ScheduleStatePanelContent =
        loadingPanelResources().toContent()

    @Composable
    fun emptyPanelContent(): ScheduleStatePanelContent =
        emptyPanelResources().toContent()

    @Composable
    fun statePanelContent(message: ScheduleUiMessage): ScheduleStatePanelContent =
        statePanelResources(message).toContent()

    @Composable
    fun weekLabel(weekNumber: Int): String =
        stringResource(Res.string.schedule_week_label, weekNumber)

    @Composable
    fun raceCount(count: Int): String =
        pluralStringResource(Res.plurals.schedule_race_count, count, count)

    @Composable
    fun loadingDateContext(): String =
        stringResource(Res.string.schedule_week_dates_loading)

    suspend fun loadWeekTitle(weekNumber: Int): String =
        getString(Res.string.schedule_week_title, weekNumber)

    suspend fun loadLastUpdated(time: String): String =
        getString(Res.string.schedule_last_updated, time)

    suspend fun loadRaceCount(count: Int): String =
        getPluralString(Res.plurals.schedule_race_count, count, count)

    @Composable
    private fun ScheduleStatePanelResources.toContent(): ScheduleStatePanelContent =
        ScheduleStatePanelContent(
            variant = variant,
            title = stringResource(title),
            message = stringResource(message),
            retryLabel = retryLabel?.let { stringResource(it) },
        )
}

data class ScheduleBottomTabResources(
    val label: StringResource,
    val icon: DrawableResource,
    val selected: Boolean,
    val enabled: Boolean,
)

data class ScheduleStatePanelResources(
    val variant: ScheduleStatePanelVariant,
    val title: StringResource,
    val message: StringResource,
    val retryLabel: StringResource? = null,
)
