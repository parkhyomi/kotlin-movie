package domain.model.ScreeningSchedule

import domain.model.Movie
import domain.model.seat.RowLabel
import domain.model.seat.Seat
import domain.model.seat.SeatStatus
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class ScreeningTest {
    private fun screening(
        screeningDate: LocalDate = LocalDate.of(2026, 4, 10),
        startTime: LocalTime = LocalTime.of(10, 0),
        movie: Movie = movie(title = "기본 영화", runningMinutes = 120),
    ): Screening =
        Screening(
            screeningDate = screeningDate,
            startTime = startTime,
            movie = movie,
        )

    private fun movie(
        title: String,
        runningMinutes: Int,
    ): Movie = Movie(title = title, runningMinutes = runningMinutes)

    private fun seatStatusOf(
        screening: Screening,
        targetSeat: Seat,
    ): SeatStatus =
        screening
            .seatStatuses()
            .first { seatAvailability -> seatAvailability.isSeat(targetSeat) }
            .status

    @Test
    fun `종료 시각은 시작 시각 + 영화 러닝타임으로 계산된다`() {
        val screening =
            screening(
                startTime = LocalTime.of(10, 0),
                movie = movie(title = "탑건: 매버릭", runningMinutes = 130),
            )

        Assertions.assertThat(screening.endTime).isEqualTo(LocalTime.of(12, 10))
    }

    @Test
    fun `영화 러닝타임이 0분 이하이면 상영은 생성되지 않는다`() {
        Assertions
            .assertThatThrownBy {
                screening(movie = movie(title = "러닝타임 오류", runningMinutes = 0))
            }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("0보다 커야")
    }

    @Test
    fun `하루 스케줄 영화관 영업시간은 0~24시로 상영 종료 시간이 24시를 넘어가면 생성되지 않는다`() {
        Assertions
            .assertThatThrownBy {
                screening(
                    startTime = LocalTime.of(23, 30),
                    movie = movie(title = "심야 상영", runningMinutes = 120),
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("시작 시간 이후")
    }

    @Test
    fun `상영 날짜가 같으면 isOn은 true를 반환한다`() {
        val screening = screening(screeningDate = LocalDate.of(2026, 4, 9))

        assertAll(
            { Assertions.assertThat(screening.isOn(LocalDate.of(2026, 4, 9))).isTrue() },
            { Assertions.assertThat(screening.isOn(LocalDate.of(2026, 4, 10))).isFalse() },
        )
    }

    @Test
    fun `영화 제목이 같으면 isForMovie는 true를 반환한다`() {
        val screening = screening(movie = movie(title = "아이언맨 3", runningMinutes = 122))

        assertAll(
            { Assertions.assertThat(screening.isForMovie("아이언맨 3")).isTrue() },
            { Assertions.assertThat(screening.isForMovie("마더")).isFalse() },
        )
    }

    @Test
    fun `시작 시각이 같으면 startsAt은 true를 반환한다`() {
        val screening = screening(startTime = LocalTime.of(13, 0))

        assertAll(
            { Assertions.assertThat(screening.startsAt(LocalTime.of(13, 0))).isTrue() },
            { Assertions.assertThat(screening.startsAt(LocalTime.of(13, 1))).isFalse() },
        )
    }

    @Test
    fun `여러 좌석을 한 번에 예약하면 모든 좌석이 예약 상태가 된다`() {
        val targetSeats =
            listOf(
                Seat(column = 1, row = RowLabel.C),
                Seat(column = 2, row = RowLabel.C),
                Seat(column = 3, row = RowLabel.C),
            )
        val reserved = screening().reserveAll(targetSeats)

        targetSeats.forEach { targetSeat ->
            Assertions.assertThat(seatStatusOf(reserved, targetSeat)).isEqualTo(SeatStatus.RESERVED)
        }
    }

    @Test
    fun `좌석 1개 예약 시 해당 좌석만 예약 불가 상태로 변경된다`() {
        val targetSeats = listOf(Seat(column = 1, row = RowLabel.A))
        val untouchedSeat = Seat(column = 2, row = RowLabel.A)
        val screening = screening()

        val reserved = screening.reserveAll(targetSeats)

        assertAll(
            {
                targetSeats.forEach { targetSeat ->
                    Assertions.assertThat(seatStatusOf(reserved, targetSeat)).isEqualTo(SeatStatus.RESERVED)
                }
            },
            { Assertions.assertThat(seatStatusOf(reserved, untouchedSeat)).isEqualTo(SeatStatus.AVAILABLE) },
        )

    }

    @Test
    fun `이미 예약된 좌석을 다시 예약하면 예외가 발생한다`() {
        val targetSeats = listOf(Seat(column = 3, row = RowLabel.B))
        val result = screening().reserveAll(targetSeats)

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            result.reserveAll(targetSeats)
        }
    }

    @Test
    fun `같은 날짜에서 상영 시간이 겹치면 overlapsWith는 true를 반환한다`() {
        val first =
            screening(
                screeningDate = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(10, 0),
                movie = movie(title = "영화 A", runningMinutes = 120),
            )
        val second =
            screening(
                screeningDate = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(11, 30),
                movie = movie(title = "영화 B", runningMinutes = 100),
            )

        assertAll(
            { Assertions.assertThat(first.overlapsWith(second)).isTrue() },
            { Assertions.assertThat(second.overlapsWith(first)).isTrue() },
        )
    }

    @Test
    fun `같은 날짜라도 앞 상영 종료 시각과 다음 시작 시각이 같으면 겹치지 않는다`() {
        val first =
            screening(
                screeningDate = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(10, 0),
                movie = movie(title = "영화 A", runningMinutes = 120),
            )
        val second =
            screening(
                screeningDate = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(12, 0),
                movie = movie(title = "영화 B", runningMinutes = 100),
            )

        assertAll(
            { Assertions.assertThat(first.overlapsWith(second)).isFalse() },
            { Assertions.assertThat(second.overlapsWith(first)).isFalse() },
        )
    }

    @Test
    fun `날짜가 다르면 시간이 겹쳐도 overlapsWith는 false를 반환한다`() {
        val first =
            screening(
                screeningDate = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(10, 0),
                movie = movie(title = "영화 A", runningMinutes = 120),
            )
        val second =
            screening(
                screeningDate = LocalDate.of(2026, 4, 11),
                startTime = LocalTime.of(10, 30),
                movie = movie(title = "영화 B", runningMinutes = 120),
            )

        assertAll(
            { Assertions.assertThat(first.overlapsWith(second)).isFalse() },
            { Assertions.assertThat(second.overlapsWith(first)).isFalse() },
        )
    }
}
