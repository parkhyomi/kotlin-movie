package domain.model.screeningschedule

import java.time.LocalDate
import java.time.LocalTime

data class ScreeningTemplate(
    val movieTitle: String,
    val screeningDate: LocalDate,
    val startTime: LocalTime,
)
