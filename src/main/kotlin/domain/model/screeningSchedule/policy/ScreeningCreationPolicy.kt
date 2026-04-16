package domain.model.screeningschedule.policy

import domain.model.screeningschedule.Screening
import java.time.LocalDate

interface ScreeningCreationPolicy {
    fun validate(
        candidate: Screening,
        existing: List<Screening>,
        screenPeriod: ScreenPeriod,
    )
}
