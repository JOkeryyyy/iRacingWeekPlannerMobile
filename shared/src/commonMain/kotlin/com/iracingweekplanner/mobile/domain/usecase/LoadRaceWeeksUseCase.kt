package com.iracingweekplanner.mobile.domain.usecase

import com.iracingweekplanner.mobile.domain.model.PlannerDataResult
import com.iracingweekplanner.mobile.domain.model.RaceWeek
import com.iracingweekplanner.mobile.domain.repository.PlannerScheduleRepository

class LoadRaceWeeksUseCase(
    private val repository: PlannerScheduleRepository,
) {
    suspend operator fun invoke(): PlannerDataResult<List<RaceWeek>> =
        repository.loadRaceWeeks()
}
