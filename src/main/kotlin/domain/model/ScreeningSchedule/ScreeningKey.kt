package domain.model.ScreeningSchedule

import java.time.LocalDate
import java.time.LocalTime

data class ScreeningKey(
    val movieTitle: String,
    val date: LocalDate,
    val startTime: LocalTime,
)
