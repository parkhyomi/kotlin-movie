package controller

import domain.model.screeningschedule.Screening
import domain.model.seat.SeatAvailability
import infra.db.inmemory.InMemoryScreeningRepository
import infra.db.repository.ScreeningRepository
import view.parseSeats
import java.time.LocalDate
import java.time.LocalTime

class ScreenController(
    private val screeningRepository: ScreeningRepository = InMemoryScreeningRepository(),
) {
    init {
        if (screeningRepository.findAllScreenings().isEmpty()) {
            seedDefaultScreenings()
        }
    }

    // 특정 제목의 영화 상영 목록을 조회한다.
    fun findScreeningTitle(title: String): List<Screening> = screeningRepository.screeningsOfMovieTitle(title)

    // 특정 제목 + 날짜 기준 상영 목록을 조회한다.
    fun findScreenings(
        title: String,
        date: LocalDate,
    ): List<Screening> =
        screeningRepository.screeningsOfMovieDate(
            screenings = findScreeningTitle(title),
            date = date,
        )

    // 특정 상영의 좌석 상태를 조회한다.
    fun findSeatStatuses(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability> = screeningRepository.seatStatusesOf(movieTitle, date, startTime)

    // 특정 상영에 대해 선택 좌석들을 예약 처리하고, 예약이 반영된 상영을 반환한다.
    fun reserveSeats(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seats: List<String>,
    ): Screening =
        screeningRepository.reserveSeats(
            movieTitle = movieTitle,
            date = date,
            startTime = startTime,
            seats = parseSeats(seats),
        )

    private fun seedDefaultScreenings() {
        val seeds =
            listOf(
                ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0)),
                ScreeningSeed("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0)),
                ScreeningSeed("아이언맨 3", LocalDate.of(2026, 4, 6), LocalTime.of(16, 0)),
                ScreeningSeed("아이언맨 3", LocalDate.of(2026, 4, 6), LocalTime.of(19, 0)),
                ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 7), LocalTime.of(10, 0)),
                ScreeningSeed("스파이더맨: 노 웨이 홈", LocalDate.of(2026, 4, 7), LocalTime.of(13, 30)),
                ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 8), LocalTime.of(10, 0)),
                ScreeningSeed("남은 인생 10년", LocalDate.of(2026, 4, 8), LocalTime.of(14, 0)),
                ScreeningSeed("오늘 밤 이세상에서 사랑이 사라진다 해도", LocalDate.of(2026, 4, 9), LocalTime.of(12, 20)),
                ScreeningSeed("아이언맨 3", LocalDate.of(2026, 4, 10), LocalTime.of(9, 50)),
                ScreeningSeed("체인소맨", LocalDate.of(2026, 4, 10), LocalTime.of(16, 0)),
                ScreeningSeed("호퍼스", LocalDate.of(2026, 4, 11), LocalTime.of(20, 10)),
                ScreeningSeed("스파이더맨: 노 웨이 홈", LocalDate.of(2026, 4, 12), LocalTime.of(15, 40)),
                ScreeningSeed("마더", LocalDate.of(2026, 4, 13), LocalTime.of(11, 0)),
            )

        seeds.forEach { seed ->
            screeningRepository.createScreening(
                movieTitle = seed.movieTitle,
                screeningDate = seed.screeningDate,
                startTime = seed.startTime,
            )
        }
    }

    private data class ScreeningSeed(
        val movieTitle: String,
        val screeningDate: LocalDate,
        val startTime: LocalTime,
    )
}
