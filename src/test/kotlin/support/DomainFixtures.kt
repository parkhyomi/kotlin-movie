package support

import domain.model.movie.Movie
import domain.model.screeningschedule.Screening
import domain.model.seat.RowLabel
import domain.model.seat.Seat
import java.time.LocalDate
import java.time.LocalTime

fun movieFixture(
    title: String = "테스트 영화",
    runningMinutes: Int = 120,
): Movie = Movie(title = title, runningMinutes = runningMinutes)

fun screeningFixture(
    date: LocalDate = LocalDate.of(2026, 4, 10),
    startTime: LocalTime = LocalTime.of(10, 0),
    title: String = "테스트 영화",
    runningMinutes: Int = 120,
): Screening =
    Screening(
        screeningDate = date,
        startTime = startTime,
        movie = movieFixture(title = title, runningMinutes = runningMinutes),
    )

fun seatFixture(
    row: RowLabel,
    column: Int,
): Seat = Seat(column = column, row = row)
