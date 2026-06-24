package com.iracingweekplanner.mobile

import android.app.Application
import com.iracingweekplanner.mobile.platform.AppDependencies
import com.iracingweekplanner.mobile.platform.createAppDependencies

class IRacingWeekPlannerApplication : Application() {
    lateinit var appDependencies: AppDependencies
        private set

    override fun onCreate() {
        super.onCreate()
        appDependencies = createAppDependencies(this)
    }

    override fun onTerminate() {
        if (::appDependencies.isInitialized) {
            appDependencies.close()
        }
        super.onTerminate()
    }
}
