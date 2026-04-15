package domain.model.screeningschedule

import java.time.LocalDate
import java.time.LocalTime

data class ScreeningKey(
    val movieTitle: String,
    val date: LocalDate,
    val startTime: LocalTime,
)
