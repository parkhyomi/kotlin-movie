package domain.model.screeningschedule

import domain.model.Movie.Movie
import domain.model.screeningschedule.policy.DefaultScreeningCreationPolicy
import domain.model.screeningschedule.policy.ScreenPeriod
import domain.model.screeningschedule.policy.ScreeningCreationPolicy
import domain.model.seat.Seat
import domain.model.seat.SeatAvailability
import java.time.LocalDate
import java.time.LocalTime

class ScreeningSchedule(
    private val movies: List<Movie>,
    private val screenPeriod: ScreenPeriod,
    screenings: List<Screening> = emptyList(),
    private val screeningCreationPolicy: ScreeningCreationPolicy = DefaultScreeningCreationPolicy(),
) {

    private val screenings: MutableList<Screening> = screenings.toMutableList()

    fun screeningsOfMovieTitle(movieTitle: String): List<Screening> =
        screenings.filter { screening ->
            screening.isForMovie(movieTitle)
        }

    fun screeningsOfMovieDate(
        screenings: List<Screening>,
        date: LocalDate,
    ): List<Screening> =
        screenings.filter { screening ->
            screening.isOn(date)
        }

    fun seatStatusesOf(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability> =
        screeningOf(
            movieTitle = movieTitle,
            date = date,
            startTime = startTime,
        ).seatStatuses()

    fun reserveSeats(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seats: List<Seat>,
    ): Screening {
        val source =
            screeningOf(
                movieTitle = movieTitle,
                date = date,
                startTime = startTime,
            )
        val target = source.reserveAll(seats)
        replace(source, target)
        return target
    }

    fun createScreening(
        movieTitle: String,
        screeningDate: LocalDate,
        startTime: LocalTime,
    ): Screening {
        val movie = findMovie(movieTitle)
        val newScreening =
            Screening(
                screeningDate = screeningDate,
                startTime = startTime,
                movie = movie,
            )

        screeningCreationPolicy.validate(
            candidate = newScreening,
            existing = screenings.toList(),
            screenPeriod= screenPeriod,
        )
        screenings.add(newScreening)
        return newScreening
    }

    private fun screeningOf(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): Screening =
        screeningsOfMovieDate(screeningsOfMovieTitle(movieTitle), date)
            .firstOrNull { screening ->
                screening.startsAt(startTime)
            }
            ?: throw IllegalArgumentException("해당 조건의 상영이 존재하지 않습니다.")

    private fun replace(
        source: Screening,
        target: Screening,
    ) {
        val index = screenings.indexOf(source)
        require(index >= 0) { "해당 상영을 찾을 수 없습니다." }
        screenings[index] = target
    }

    private fun findMovie(title: String): Movie =
        movies.firstOrNull { movie ->
            movie.findMovieTitle() == title
        } ?: throw IllegalArgumentException("존재하지 않는 영화입니다.")
}
