package infra.db.repository

import domain.model.screeningschedule.Screening
import domain.model.seat.Seat
import domain.model.seat.SeatAvailability
import java.time.LocalDate
import java.time.LocalTime

interface ScreeningRepository {
    fun findAllScreenings(): List<Screening>

    fun saveAll(screenings: List<Screening>)

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
