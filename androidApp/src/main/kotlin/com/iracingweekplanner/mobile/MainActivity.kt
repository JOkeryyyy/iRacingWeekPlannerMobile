package com.iracingweekplanner.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iracingweekplanner.mobile.presentation.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val appDependencies = iracingWeekPlannerApplication.appDependencies

        setContent {
            App(stateHolder = appDependencies.appInfoStateHolder)
        }
    }

    private val iracingWeekPlannerApplication: IRacingWeekPlannerApplication
        get() = application as IRacingWeekPlannerApplication
}
