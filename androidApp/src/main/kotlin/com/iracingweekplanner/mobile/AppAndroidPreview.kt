package com.iracingweekplanner.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.platform.createAppDependencies
import com.iracingweekplanner.mobile.presentation.App

@Preview
@Composable
fun AppAndroidPreview() {
    val context = LocalContext.current
    val appDependencies = remember(context) { createAppDependencies(context) }
    DisposableEffect(appDependencies) {
        onDispose { appDependencies.close() }
    }
    App(stateHolder = appDependencies.appInfoStateHolder)
}
