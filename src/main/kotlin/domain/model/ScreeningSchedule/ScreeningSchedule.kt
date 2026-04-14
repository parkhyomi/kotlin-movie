package domain.model.ScreeningSchedule

import domain.model.Movie
import domain.model.ScreeningSchedule.policy.DefaultScreeningCreationPolicy
import domain.model.ScreeningSchedule.policy.ScreeningCreationPolicy
import domain.model.seat.Seat
import domain.model.seat.SeatAvailability
import view.ScreeningTemplate
import java.time.LocalDate
import java.time.LocalTime

// 상영 기간(날짜 범위) 내에서 실제 상영(Screening) 목록을 관리한다.
class ScreeningSchedule(
    private val movies: List<Movie>,
    private val screeningPeriodStart: LocalDate,
    private val screeningPeriodEnd: LocalDate,
    screenings: List<Screening> = emptyList(),
    private val screeningCreationPolicy: ScreeningCreationPolicy = DefaultScreeningCreationPolicy(),
) : ScreeningReader, ScreeningWriter {
    private val screenings: MutableList<Screening> = screenings.toMutableList()

    init {
        require(!screeningPeriodEnd.isBefore(screeningPeriodStart)) { "상영 기간 종료일은 시작일보다 빠를 수 없습니다." }
    }

    override fun screeningsOfMovieTitle(movieTitle: String): List<Screening> =
        screenings.filter { screening ->
            screening.isForMovie(movieTitle)
        }

    override fun screeningsOfMovieDate(
        screenings: List<Screening>,
        date: LocalDate,
    ): List<Screening> =
        screenings.filter { screening ->
            screening.isOn(date)
        }

    override fun seatStatusesOf(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability> =
        screeningOf(
            ScreeningKey(
                movieTitle = movieTitle,
                date = date,
                startTime = startTime,
            ),
        ).seatStatuses()

    override fun reserveSeats(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seats: List<Seat>,
    ): Screening {
        val source =
            screeningOf(
                ScreeningKey(
                    movieTitle = movieTitle,
                    date = date,
                    startTime = startTime,
                ),
            )
        val target = source.reserveAll(seats)
        replace(source, target)
        return target
    }

    override fun createScreening(
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
            periodStart = screeningPeriodStart,
            periodEnd = screeningPeriodEnd,
        )
        screenings.add(newScreening)
        return newScreening
    }

    private fun screeningOf(key: ScreeningKey): Screening =
        screeningsOfMovieDate(screeningsOfMovieTitle(key.movieTitle), key.date)
            .firstOrNull { screening ->
                screening.startsAt(key.startTime)
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

    // 제목으로 영화를 찾는다.
    private fun findMovie(title: String): Movie =
        movies.firstOrNull { movie ->
            movie.title == title
        } ?: throw IllegalArgumentException("존재하지 않는 영화입니다.")

    companion object {
        // 임의 영화 목록 + 임의 상영 샘플 값으로 초기 스케줄을 만든다.
        fun withSamples(
            screeningPeriodStart: LocalDate,
            screeningPeriodEnd: LocalDate,
            movies: List<Movie>,
            samples: List<ScreeningTemplate>,
        ): ScreeningSchedule {
            val screeningSchedule =
                ScreeningSchedule(
                    movies = movies,
                    screeningPeriodStart = screeningPeriodStart,
                    screeningPeriodEnd = screeningPeriodEnd,
                )

            // 샘플을 순회하면서 실제 상영을 생성하고 내부 스케줄에 누적한다.
            samples.forEach { sample ->
                screeningSchedule.createScreening(
                    movieTitle = sample.movieTitle,
                    screeningDate = sample.screeningDate,
                    startTime = sample.startTime,
                )
            }
            return screeningSchedule
        }
    }
}
