package controller

import domain.model.Movie
import domain.model.screen.ScreeningSchedule
import view.defaultScreeningSeeds
import domain.model.screen.Screening
import domain.model.seat.SeatAvailability
import domain.parseSeats
import java.time.LocalDate
import java.time.LocalTime

// 영화관 전체 도메인 조합 객체
class ScreenController(
    // 스케줄 상영 기간 (예: 4/6 ~ 4/13)
    private val screeningPeriodStart: LocalDate = LocalDate.of(2026, 4, 6),
    private val screeningPeriodEnd: LocalDate = LocalDate.of(2026, 4, 13),
    private val screeningSchedule: ScreeningSchedule =
        ScreeningSchedule.withSamples(
            screeningPeriodStart = screeningPeriodStart,
            screeningPeriodEnd = screeningPeriodEnd,
            movies = Movie.sampleMovies,
            samples = defaultScreeningSeeds(),
        ),
) {
    // 특정 제목의 영화 상영 목록을 조회한다.
    fun findScreeningTitle(title: String): List<Screening> = screeningSchedule.screeningsOfMovieTitle(title)

    // 특정 날짜의 특정 영화 상영 목록을 조회한다.
    fun findScreeningsDate(
        screenings: List<Screening>,
        date: LocalDate,
    ): List<Screening> = screeningSchedule.screeningsOfMovieDate(screenings, date)

    // 특정 상영의 좌석 상태를 조회한다.
    fun findSeatStatuses(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability> = screeningSchedule.seatStatusesOf(movieTitle, date, startTime)

    // 특정 상영에 대해 선택 좌석들을 예약 처리하고, 예약이 반영된 상영을 반환한다.
    fun reserveSeats(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seats: List<String>,
    ): Screening =
        screeningSchedule.reserveSeats(
            movieTitle = movieTitle,
            date = date,
            startTime = startTime,
            seats = parseSeats(seats),
        )
}
