package com.iracingweekplanner.mobile

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.remember
import com.iracingweekplanner.mobile.platform.createAppDependencies
import com.iracingweekplanner.mobile.presentation.App

fun MainViewController() = ComposeUIViewController {
    val appDependencies = remember { createAppDependencies() }
    App(
        loadPlannerData = appDependencies.loadPlannerData,
    )
}
