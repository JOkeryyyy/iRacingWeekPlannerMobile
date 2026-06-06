package com.iracingweekplanner.mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform