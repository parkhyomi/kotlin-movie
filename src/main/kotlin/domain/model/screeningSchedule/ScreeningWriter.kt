package domain.model.screeningschedule

import domain.model.seat.Seat
import java.time.LocalDate
import java.time.LocalTime

interface ScreeningWriter {
    fun reserveSeats(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seats: List<Seat>,
    ): Screening

    fun createScreening(
        movieTitle: String,
        screeningDate: LocalDate,
        startTime: LocalTime,
    ): Screening
}
