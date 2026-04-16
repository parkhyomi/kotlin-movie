package controller

import domain.model.Movie.Movie
import domain.model.screeningschedule.Screening
import domain.model.screeningschedule.ScreeningFactory
import domain.model.screeningschedule.ScreeningReader
import domain.model.screeningschedule.ScreeningSchedule
import domain.model.screeningschedule.ScreeningWriter
import domain.model.seat.SeatAvailability
import view.parseSeats
import view.defaultScreeningSeeds
import java.time.LocalDate
import java.time.LocalTime

class ScreenController(
    private val screeningPeriodStart: LocalDate = LocalDate.of(2026, 4, 6),
    private val screeningPeriodEnd: LocalDate = LocalDate.of(2026, 4, 13),
    private val screeningSchedule: ScreeningSchedule =
        ScreeningFactory().withSamples(
            screeningPeriodStart = screeningPeriodStart,
            screeningPeriodEnd = screeningPeriodEnd,
            movies = Movie.sampleMovies,
            samples = defaultScreeningSeeds(),
        ),
    private val screeningReader: ScreeningReader = screeningSchedule,
    private val screeningWriter: ScreeningWriter = screeningSchedule,
) {
    // 특정 제목의 영화 상영 목록을 조회한다.
    fun findScreeningTitle(title: String): List<Screening> = screeningReader.screeningsOfMovieTitle(title)

    // 특정 제목 + 날짜 기준 상영 목록을 조회한다.
    fun findScreenings(
        title: String,
        date: LocalDate,
    ): List<Screening> = screeningReader.screeningsOf(title, date)

    // 특정 상영의 좌석 상태를 조회한다.
    fun findSeatStatuses(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability> = screeningReader.seatStatusesOf(movieTitle, date, startTime)

    // 특정 상영에 대해 선택 좌석들을 예약 처리하고, 예약이 반영된 상영을 반환한다.
    fun reserveSeats(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seats: List<String>,
    ): Screening =
        screeningWriter.reserveSeats(
            movieTitle = movieTitle,
            date = date,
            startTime = startTime,
            seats = parseSeats(seats),
        )
}
