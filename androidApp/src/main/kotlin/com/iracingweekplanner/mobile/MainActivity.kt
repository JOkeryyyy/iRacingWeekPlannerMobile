package com.iracingweekplanner.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.iracingweekplanner.mobile.platform.AppDependencies
import com.iracingweekplanner.mobile.platform.createAppDependencies
import com.iracingweekplanner.mobile.presentation.App

class MainActivity : ComponentActivity() {
    private lateinit var appDependencies: AppDependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        appDependencies = createAppDependencies(this)

        setContent {
            App(stateHolder = appDependencies.appInfoStateHolder)
        }
    }

    override fun onDestroy() {
        appDependencies.close()
        super.onDestroy()
    }
}

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
