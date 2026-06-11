package com.iracingweekplanner.mobile

import androidx.compose.ui.window.ComposeUIViewController
import com.iracingweekplanner.mobile.platform.createAppInfoStateHolder
import com.iracingweekplanner.mobile.presentation.App

fun MainViewController() = ComposeUIViewController {
    App(stateHolder = createAppInfoStateHolder())
}
