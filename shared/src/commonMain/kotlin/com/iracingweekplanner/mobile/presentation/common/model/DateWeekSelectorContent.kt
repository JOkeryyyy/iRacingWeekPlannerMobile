package com.iracingweekplanner.mobile.presentation.common.model

data class DateWeekSelectorContent(
    val weekLabel: String,
    val dateContext: String,
    val previousEnabled: Boolean,
    val nextEnabled: Boolean,
    val previousLabel: String,
    val previousContentDescription: String,
    val todayLabel: String,
    val nextLabel: String,
    val nextContentDescription: String,
)
