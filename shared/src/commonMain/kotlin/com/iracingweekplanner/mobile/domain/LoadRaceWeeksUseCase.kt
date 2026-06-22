package com.iracingweekplanner.mobile.domain

class LoadRaceWeeksUseCase(
    private val repository: PlannerScheduleRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<List<RaceWeek>> =
        repository.loadRaceWeeks()
}
