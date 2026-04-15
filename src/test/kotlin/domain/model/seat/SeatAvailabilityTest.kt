package domain.model.seat

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SeatAvailabilityTest {
    private val seat = Seat(3, RowLabel.A)
    private val seatAvailability = SeatAvailability(seat)

    @Test
    fun `요청한 좌석과 동일한 좌석이 있다면 true를 반환한다`() {
        val targetSeat = Seat(3, RowLabel.A)
        val result = seatAvailability.isSeat(targetSeat)

        assertThat(result).isTrue()
    }

    @Test
    fun `요청한 좌석과 동일한 좌석이 없다면 false 반환한다`() {
        val targetSeat = Seat(7, RowLabel.A)
        val result = seatAvailability.isSeat(targetSeat)

        assertThat(result).isFalse()
    }

    @Test
    fun `선택한 좌석이 예약이 가능하다면 true를 반환한다`() {
        val result = seatAvailability.isAvailable()

        assertThat(result).isTrue()
    }

    @Test
    fun `선택한 좌석이 예약이 불가능하다면 false를 반환한다`() {
        val seatAvailability = SeatAvailability(seat, SeatStatus.RESERVED)

        val result = seatAvailability.isAvailable()

        assertThat(result).isFalse()
    }

    @Test
    fun `해당 좌석의 예약 상태가 변경된다`() {
        val result = seatAvailability.reserve()

        assertThat(result.status).isEqualTo(SeatStatus.RESERVED)
    }
}
