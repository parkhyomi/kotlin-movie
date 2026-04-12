package domain.model.schedule

import domain.model.Movie
import domain.model.screen.Screening
import domain.model.screen.ScreeningSchedule
import domain.model.screen.ScreeningTemplate
import domain.model.seat.RowLabel
import domain.model.seat.Seat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class ScreeningScheduleTest {
    private fun movieSchedule(
        periodStart: LocalDate = LocalDate.of(2026, 4, 6),
        periodEnd: LocalDate = LocalDate.of(2026, 4, 13),
        movies: List<Movie> = testMovies(),
        screenings: List<Screening> = emptyList(),
    ): ScreeningSchedule =
        ScreeningSchedule(
            movies = movies,
            screeningPeriodStart = periodStart,
            screeningPeriodEnd = periodEnd,
            screenings = screenings,
        )

    private fun sample(
        movieTitle: String,
        date: LocalDate,
        time: LocalTime,
    ): ScreeningTemplate =
        ScreeningTemplate(
            movieTitle = movieTitle,
            screeningDate = date,
            startTime = time,
        )

    private fun screening(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): Screening =
        Screening(
            screeningDate = date,
            startTime = startTime,
            movie = testMovies().first { movie -> movie.title == movieTitle },
        )

    private fun testMovies(): List<Movie> =
        listOf(
            Movie(title = "탑건: 매버릭", runningMinutes = 130),
            Movie(title = "마더", runningMinutes = 100),
            Movie(title = "아이언맨 3", runningMinutes = 122),
        )

    @Test
    fun `상영 기간 종료일이 시작일보다 빠르면 스케줄을 생성할 수 없다`() {
        assertThrows(IllegalArgumentException::class.java) {
            movieSchedule(
                periodStart = LocalDate.of(2026, 4, 10),
                periodEnd = LocalDate.of(2026, 4, 9),
            )
        }
    }

    @Test
    fun `screeningsOn은 전달한 날짜의 상영만 반환한다`() {
        val schedule =
            movieSchedule(
                screenings =
                    listOf(
                        screening("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0)),
                        screening("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0)),
                        screening("아이언맨 3", LocalDate.of(2026, 4, 7), LocalTime.of(16, 0)),
                    ),
            )

        val screenings = schedule.screeningsOn(date = LocalDate.of(2026, 4, 6))

        assertThat(screenings.size).isEqualTo(2)
        assertThat(screenings.map { screening -> screening.movie.title })
            .isEqualTo(listOf("탑건: 매버릭", "마더"))
    }

    @Test
    fun `screeningsOfMovieDate는 날짜 + 제목이 일치하는 상영만 반환한다`() {
        val schedule =
            movieSchedule(
                screenings =
                    listOf(
                        screening("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0)),
                        screening("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(14, 0)),
                        screening("탑건: 매버릭", LocalDate.of(2026, 4, 7), LocalTime.of(10, 0)),
                        screening("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0)),
                    ),
            )
        val screeningsForTitle = schedule.screeningsOfMovieTitle("탑건: 매버릭")

        val screenings = schedule.screeningsOfMovieDate(screeningsForTitle, LocalDate.of(2026, 4, 6))

        assertThat(screenings.size).isEqualTo(2)
        assertThat(screenings.map { screening -> screening.startTime })
            .isEqualTo(listOf(LocalTime.of(10, 0), LocalTime.of(14, 0)))
    }

    @Test
    fun `screeningsOfMovieDate는 상영 시작 시간 목록만 순서대로 반환한다`() {
        val schedule =
            movieSchedule(
                screenings =
                    listOf(
                        screening("마더", LocalDate.of(2026, 4, 8), LocalTime.of(9, 30)),
                        screening("마더", LocalDate.of(2026, 4, 8), LocalTime.of(12, 0)),
                        screening("마더", LocalDate.of(2026, 4, 8), LocalTime.of(16, 10)),
                    ),
            )
        val screeningsForTitle = schedule.screeningsOfMovieTitle("마더")

        val screenings = schedule.screeningsOfMovieDate(screeningsForTitle, LocalDate.of(2026, 4, 8))

        assertThat(screenings.map { screening -> screening.startTime }).isEqualTo(
            listOf(
                LocalTime.of(9, 30),
                LocalTime.of(12, 0),
                LocalTime.of(16, 10),
            ),
        )
    }

    @Test
    fun `seatStatusesOf는 해당 상영의 전체 좌석 상태를 반환한다`() {
        val schedule =
            movieSchedule(
                screenings =
                    listOf(
                        screening("아이언맨 3", LocalDate.of(2026, 4, 9), LocalTime.of(10, 0)),
                    ),
            )

        val statuses = schedule.seatStatusesOf("아이언맨 3", LocalDate.of(2026, 4, 9), LocalTime.of(10, 0))

        assertThat(statuses.size).isEqualTo(60)
        assertThat(statuses.all { seatAvailability -> seatAvailability.isAvailable() }).isTrue()
    }

    @Test
    fun `없는 상영을 seatStatusesOf로 조회하면 예외가 발생한다`() {
        val schedule = movieSchedule(screenings = emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            schedule.seatStatusesOf("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0))
        }
    }

    @Test
    fun `reserveSeats를 호출하면 해당 상영 좌석이 예약 상태로 반영된 Screening을 반환한다`() {
        val schedule =
            movieSchedule(
                screenings =
                    listOf(
                        screening("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0)),
                    ),
            )
        val firstSeat = Seat(column = 1, row = RowLabel.A)
        val secondSeat = Seat(column = 2, row = RowLabel.B)

        val reserved =
            schedule.reserveSeats(
                movieTitle = "탑건: 매버릭",
                date = LocalDate.of(2026, 4, 6),
                startTime = LocalTime.of(10, 0),
                seats = listOf(firstSeat, secondSeat),
            )

        assertThat(reserved.isAvailable(firstSeat)).isFalse()
        assertThat(reserved.isAvailable(secondSeat)).isFalse()
    }

    @Test
    fun `이미 예약된 좌석을 다시 reserveSeats 하면 예외가 발생한다`() {
        val schedule =
            movieSchedule(
                screenings =
                    listOf(
                        screening("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0)),
                    ),
            )
        val seats = listOf(Seat(column = 3, row = RowLabel.C))

        val reserved = schedule.reserveSeats("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0), seats)

        assertThrows(IllegalArgumentException::class.java) {
            reserved.reserveAll(seats)
        }
    }

    @Test
    fun `createScreening은 상영 기간 안에서 상영 객체를 생성한다`() {
        val schedule = movieSchedule(screenings = emptyList())

        val screening =
            schedule.createScreening(
                movieTitle = "탑건: 매버릭",
                screeningDate = LocalDate.of(2026, 4, 7),
                startTime = LocalTime.of(11, 0),
            )

        assertThat(screening.movie.title).isEqualTo("탑건: 매버릭")
        assertThat(screening.startTime).isEqualTo(LocalTime.of(11, 0))
        assertThat(screening.endTime).isEqualTo(LocalTime.of(13, 10))
    }

    @Test
    fun `createScreening에서 상영 기간 밖 날짜를 등록하면 예외가 발생한다`() {
        val schedule = movieSchedule(screenings = emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            schedule.createScreening(
                movieTitle = "탑건: 매버릭",
                screeningDate = LocalDate.of(2026, 4, 20),
                startTime = LocalTime.of(10, 0),
            )
        }
    }

    @Test
    fun `createScreening에서 존재하지 않는 영화 제목이면 예외가 발생한다`() {
        val schedule = movieSchedule(screenings = emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            schedule.createScreening(
                movieTitle = "없는 영화",
                screeningDate = LocalDate.of(2026, 4, 7),
                startTime = LocalTime.of(10, 0),
            )
        }
    }

    @Test
    fun `같은 영화 상영 시간이 겹치면 createScreening은 예외를 던진다`() {
        val schedule =
            movieSchedule(
                screenings =
                    listOf(
                        screening("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0)),
                    ),
            )

        assertThrows(IllegalArgumentException::class.java) {
            schedule.createScreening(
                movieTitle = "탑건: 매버릭",
                screeningDate = LocalDate.of(2026, 4, 6),
                startTime = LocalTime.of(11, 0),
            )
        }
    }

    @Test
    fun `같은 영화라도 앞 상영 종료 시각과 다음 시작 시각이 같으면 등록 가능하다`() {
        val schedule =
            movieSchedule(
                screenings =
                    listOf(
                        screening("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0)),
                    ),
            )

        val created =
            schedule.createScreening(
                movieTitle = "탑건: 매버릭",
                screeningDate = LocalDate.of(2026, 4, 6),
                startTime = LocalTime.of(12, 10),
            )

        assertThat(created.startTime).isEqualTo(LocalTime.of(12, 10))
    }

    @Test
    fun `withSamples의 샘플에 없는 영화 제목이 있으면 예외가 발생한다`() {
        assertThrows(IllegalArgumentException::class.java) {
            ScreeningSchedule.withSamples(
                screeningPeriodStart = LocalDate.of(2026, 4, 6),
                screeningPeriodEnd = LocalDate.of(2026, 4, 13),
                movies = testMovies(),
                samples =
                    listOf(
                        sample("없는 영화", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0)),
                    ),
            )
        }
    }

    @Test
    fun `withSamples의 샘플 날짜가 상영 기간 밖이면 예외가 발생한다`() {
        assertThrows(IllegalArgumentException::class.java) {
            ScreeningSchedule.withSamples(
                screeningPeriodStart = LocalDate.of(2026, 4, 6),
                screeningPeriodEnd = LocalDate.of(2026, 4, 13),
                movies = testMovies(),
                samples =
                    listOf(
                        sample("탑건: 매버릭", LocalDate.of(2026, 4, 20), LocalTime.of(10, 0)),
                    ),
            )
        }
    }
}
