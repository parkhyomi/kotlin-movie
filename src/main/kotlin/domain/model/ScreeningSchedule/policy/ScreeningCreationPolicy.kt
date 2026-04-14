package domain.model.ScreeningSchedule.policy

import domain.model.ScreeningSchedule.Screening
import java.time.LocalDate

interface ScreeningCreationPolicy {
    fun validate(
        candidate: Screening,
        existing: List<Screening>,
        periodStart: LocalDate,
        periodEnd: LocalDate,
    )
}