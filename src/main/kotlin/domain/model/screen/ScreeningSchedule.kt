package domain.model.screen

import domain.model.Movie
import domain.model.seat.Seat
import domain.model.seat.SeatAvailability
import java.time.LocalDate
import java.time.LocalTime

// 상영 기간(날짜 범위) 내에서 실제 상영(Screening) 목록을 관리한다.
class ScreeningSchedule(
    // 스케줄이 참조할 영화 목록(기본값: Movie.sampleMovies)
    private val movies: List<Movie> = Movie.sampleMovies,
    // 스케줄 리스트가 될 시작 날짜
    private val screeningPeriodStart: LocalDate,
    // 스케줄 리스트가 될 끝 날자
    private val screeningPeriodEnd: LocalDate,
    // 현재 편성된 실제 상영 목록
    screenings: List<Screening> = listOf(),
) {
    // 생성 시 전달된 상영 목록을 내부에서 변경 가능한 상태로 보관한다.
    private val screenings: MutableList<Screening> = screenings.toMutableList()

    init {
        require(!screeningPeriodEnd.isBefore(screeningPeriodStart)) { "상영 기간 종료일은 시작일보다 빠를 수 없습니다." }
    }

    // 스케줄의 리스트 (일주일치) 상영 정보를 토대로 -> 날짜별 스크린리스트
    fun screeningsOn(date: LocalDate): List<Screening> =
        screenings.filter { screening ->
            screening.isOn(date)
        }

    // 제목을 입력받고 -> 스크린 리스트 중 해당 제목의 스크린 리스트
    fun screeningsOfMovieTitle(movieTitle: String): List<Screening> =
        screenings.filter { screening ->
            screening.isForMovie(movieTitle)
        }

    // 영화를 보고싶은 날짜를 받아 해당 스케줄 조회 -> 스크린 리스트
    fun screeningsOfMovieDate(
        screenings: List<Screening>,
        date: LocalDate,
    ): List<Screening> =
        screenings.filter { screening ->
            screening.isOn(date)
        }

    // 사용자가 고른 시간대의 좌석을 보여줌 -> 스크린에서 좌석 값을 뽑아와야 함
    fun seatStatusesOf(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability> = screeningOf(movieTitle, date, startTime).seatStatuses()

    // 예매 정보 스크린에게서 부터 가져온다 -> 예약 반영된 상영으로 스케줄을 갱신한다.
    fun reserveSeats(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seats: List<Seat>,
    ): Screening {
        val source = screeningOf(movieTitle, date, startTime)
        val target = source.reserveAll(seats)
        replace(source, target)
        return target
    }

    // 영화를 찾고, 입력받은 날짜/시작 시간으로 상영을 만든다.
    // 종료 시각은 Screening에서 movie.runningMinutes로 계산한다.
    fun createScreening(
        movieTitle: String,
        screeningDate: LocalDate,
        startTime: LocalTime,
    ): Screening {
        require(isInPeriod(screeningDate)) { "상영 기간 밖의 날짜입니다." }
        val movie = findMovie(movieTitle)
        val newScreening =
            Screening(
                screeningDate = screeningDate,
                startTime = startTime,
                movie = movie,
            )

        require(hasNoOverlapForSameMovie(newScreening)) { "같은 영화의 상영 시간이 겹칩니다." }
        screenings.add(newScreening)
        return newScreening
    }

    // 제목, 날짜, 시작 시각으로 상영 1건을 식별한다.
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

    // 예약 반영된 상영으로 기존 상영을 교체한다.
    private fun replace(
        source: Screening,
        target: Screening,
    ) {
        val index = screenings.indexOf(source)
        require(index >= 0) { "해당 상영을 찾을 수 없습니다." }
        screenings[index] = target
    }

    // 상영일이 스케줄 상영 기간 안에 있는지 확인한다.
    private fun isInPeriod(date: LocalDate): Boolean = !date.isBefore(screeningPeriodStart) && !date.isAfter(screeningPeriodEnd)

    // 같은 영화의 기존 상영과 신규 상영 시간이 겹치지 않는지 확인한다.
    private fun hasNoOverlapForSameMovie(newScreening: Screening): Boolean =
        screenings
            .filter { screening ->
                screening.isForMovie(newScreening.movie.title)
            }.none { screening ->
                screening.overlapsWith(newScreening)
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

// 제목 + 날짜 + 시작 시간으로 상영 1건을 만들기 위한 샘플 값
data class ScreeningTemplate(
    val movieTitle: String,
    val screeningDate: LocalDate,
    val startTime: LocalTime,
)
