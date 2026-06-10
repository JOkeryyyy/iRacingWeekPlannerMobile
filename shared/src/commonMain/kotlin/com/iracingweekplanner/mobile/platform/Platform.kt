package com.iracingweekplanner.mobile.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
