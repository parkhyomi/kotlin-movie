package controller

import domain.model.seat.RowLabel
import domain.model.seat.Seat
import domain.model.seat.SeatStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class CinemaControllerFactoryTest {
    @Test
    fun `withInMemory는 기본 상영 데이터를 조회할 수 있는 컨트롤러를 반환한다`() {
        val controller = CinemaControllerFactory.withInMemory()

        val screenings = controller.findScreeningTitle("탑건: 매버릭")

        assertThat(screenings).isNotEmpty()
    }

    @Test
    fun `withJdbc는 같은 DB URL로 생성된 컨트롤러 간 예약 상태를 공유한다`() {
        val dbName = "factory_jdbc_test_${UUID.randomUUID().toString().replace("-", "")}"
        val dbUrl = "jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1"

        val first = CinemaControllerFactory.withJdbc(dbUrl)
        val date = LocalDate.of(2026, 4, 6)
        val startTime = LocalTime.of(10, 0)
        first.reserve("탑건: 매버릭", date, startTime, listOf("A1"))

        val second = CinemaControllerFactory.withJdbc(dbUrl)
        val statuses = second.findSeatStatuses("탑건: 매버릭", date, startTime)
        val targetSeat = Seat(column = 1, row = RowLabel.A)

        val targetStatus =
            statuses.first { seatAvailability -> seatAvailability.isSeat(targetSeat) }.status

        assertThat(targetStatus).isEqualTo(SeatStatus.RESERVED)
    }
}
