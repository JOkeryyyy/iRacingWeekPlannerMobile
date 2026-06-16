package com.iracingweekplanner.mobile

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.iracingweekplanner.mobile.platform.createAppDependencies
import com.iracingweekplanner.mobile.presentation.App

fun MainViewController() =
    createAppDependencies().let { appDependencies ->
        ComposeUIViewController {
            DisposableEffect(appDependencies) {
                onDispose { appDependencies.close() }
            }
            App(stateHolder = appDependencies.appInfoStateHolder)
        }
    }
