package com.iracingweekplanner.mobile.presentation.schedule.model

enum class ScheduleStatePanelVariant {
    Loading,
    Empty,
    Error,
}

data class ScheduleStatePanelContent(
    val variant: ScheduleStatePanelVariant,
    val title: String,
    val message: String,
    val retryLabel: String? = null,
) {
    val canRetry: Boolean = retryLabel != null

    companion object {
        fun loading(
            title: String = "Loading schedule",
            message: String = "Preparing race week data.",
        ): ScheduleStatePanelContent =
            ScheduleStatePanelContent(
                variant = ScheduleStatePanelVariant.Loading,
                title = title,
                message = message,
            )

        fun empty(
            title: String,
            message: String,
        ): ScheduleStatePanelContent =
            ScheduleStatePanelContent(
                variant = ScheduleStatePanelVariant.Empty,
                title = title,
                message = message,
            )

        fun error(
            title: String,
            message: String,
            retryLabel: String,
        ): ScheduleStatePanelContent =
            ScheduleStatePanelContent(
                variant = ScheduleStatePanelVariant.Error,
                title = title,
                message = message,
                retryLabel = retryLabel,
            )
    }
}
