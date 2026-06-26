package com.iracingweekplanner.mobile.presentation

import kotlinx.coroutines.flow.StateFlow

interface PlannerDataPresenter {
    val uiState: StateFlow<PlannerDataUiState>

    suspend fun onAction(action: PlannerDataAction)
}
