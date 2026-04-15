package controller

import domain.model.screeningschedule.Screening
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import support.screeningFixture
import java.time.LocalDate
import java.time.LocalTime

class ReservationControllerTest {
    private fun screening(
        date: LocalDate = LocalDate.of(2026, 4, 10),
        startTime: LocalTime = LocalTime.of(10, 0),
        title: String = "테스트 영화",
        runningMinutes: Int = 120,
    ): Screening =
        screeningFixture(
            date = date,
            startTime = startTime,
            title = title,
            runningMinutes = runningMinutes,
        )

    @Test
    fun `reserve는 좌석 코드를 공백 제거 후 대문자로 정규화해서 저장한다`() {
        val reservationController = ReservationController()

        val item =
            reservationController.reserve(
                screening = screening(),
                seatCodes = listOf(" c2 ", "a12"),
            )

        assertThat(item.seats.map { seat -> "${seat.row.name}${seat.column}" }).containsExactly(
            "C2",
            "A12",
        )
    }

    @Test
    fun `reservationSummaries는 예약된 항목을 장바구니 출력 형식으로 반환한다`() {
        val reservationController = ReservationController()
        val first =
            screening(
                date = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(13, 0),
                title = "F1 더 무비",
            )
        val second =
            screening(
                date = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(16, 0),
                title = "토이 스토리",
                runningMinutes = 100,
            )

        reservationController.reserve(first, listOf("C2", "C3"))
        reservationController.reserve(second, listOf("E2"))

        val summaries = reservationController.reservationSummaries()

        assertAll(
            { assertThat(summaries).hasSize(2) },
            { assertThat(summaries[0]).isEqualTo("- [F1 더 무비] 2026-04-10 13:00  좌석: C2, C3") },
            { assertThat(summaries[1]).isEqualTo("- [토이 스토리] 2026-04-10 16:00  좌석: E2") },
        )
    }

    @Test
    fun `이미 담긴 상영과 시간이 겹치면 hasOverlapping은 true를 반환한다`() {
        val reservationController = ReservationController()
        val reserved =
            screening(
                date = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(10, 0),
                title = "아이언맨",
                runningMinutes = 120,
            )
        reservationController.reserve(reserved, listOf("A1"))

        val overlappedCandidate =
            screening(
                date = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(11, 30),
                title = "토이 스토리",
                runningMinutes = 100,
            )

        val result = reservationController.hasOverlapping(overlappedCandidate)

        assertThat(result).isTrue()
    }
}
