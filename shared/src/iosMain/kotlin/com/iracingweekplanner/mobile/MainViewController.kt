package com.iracingweekplanner.mobile

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.iracingweekplanner.mobile.platform.createAppDependencies
import com.iracingweekplanner.mobile.presentation.App

fun MainViewController(
    hostedManifestUrl: String? = null,
) = ComposeUIViewController {
    val appDependencies = remember(hostedManifestUrl) {
        createAppDependencies(hostedManifestUrl = hostedManifestUrl)
    }
    DisposableEffect(appDependencies) {
        onDispose {
            appDependencies.close()
        }
    }

    App(
        loadPlannerData = appDependencies.loadPlannerData,
    )
}
