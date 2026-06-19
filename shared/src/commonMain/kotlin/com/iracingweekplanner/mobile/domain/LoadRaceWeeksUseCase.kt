package com.iracingweekplanner.mobile.domain

class LoadRaceWeeksUseCase(
    private val repository: PlannerScheduleRepository,
) {
    suspend operator fun invoke(): List<RaceWeek> =
        repository.loadRaceWeeks()
}
