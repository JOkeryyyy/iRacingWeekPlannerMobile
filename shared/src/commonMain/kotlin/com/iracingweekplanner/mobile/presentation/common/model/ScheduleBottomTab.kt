package com.iracingweekplanner.mobile.presentation.common.model

import org.jetbrains.compose.resources.DrawableResource

data class ScheduleBottomTab(
    val label: String,
    val selected: Boolean,
    val enabled: Boolean,
    val icon: DrawableResource,
)
