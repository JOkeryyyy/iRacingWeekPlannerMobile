package com.iracingweekplanner.mobile.presentation

import com.iracingweekplanner.mobile.domain.usecase.GetAppInfoUseCase

class AppInfoStateHolder(
    private val getAppInfo: GetAppInfoUseCase,
) {
    val uiState: AppInfoUiState
        get() {
            val info = getAppInfo()
            return AppInfoUiState(
                appName = info.name,
                sourceSet = info.sourceSet,
                statusMessage = info.statusMessage,
            )
        }
}
