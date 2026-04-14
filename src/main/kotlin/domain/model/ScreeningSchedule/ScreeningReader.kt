package domain.model.ScreeningSchedule

import domain.model.seat.SeatAvailability
import java.time.LocalDate
import java.time.LocalTime

interface ScreeningReader {
    fun screeningsOfMovieTitle(movieTitle: String): List<Screening>

    fun screeningsOfMovieDate(
        screenings: List<Screening>,
        date: LocalDate,
    ): List<Screening>

    fun screeningsOf(
        movieTitle: String,
        date: LocalDate,
    ): List<Screening> = screeningsOfMovieDate(screeningsOfMovieTitle(movieTitle), date)

    fun seatStatusesOf(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability>
}
