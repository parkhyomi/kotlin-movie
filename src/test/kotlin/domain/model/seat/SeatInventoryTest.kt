package domain.model.seat

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SeatInventoryTest {
    private fun seatStatusOf(
        inventory: SeatInventory,
        targetSeat: Seat,
    ): SeatStatus =
        inventory
            .statuses()
            .first { seatAvailability -> seatAvailability.isSeat(targetSeat) }
            .status

    @Test
    fun `한 상영관의 여러 좌석 중 target 좌석이 예약이 된다면 true를 반환한다`() {
        val seatInventory = SeatInventory(seats = SeatInventory.defaultSeatAvailabilities())
        val targetSeat = Seat(3, RowLabel.A)

        val result = seatStatusOf(seatInventory, targetSeat)

        assertThat(result).isEqualTo(SeatStatus.AVAILABLE)
    }

    @Test
    fun `한 상영관의 여러 좌석 중 target 좌석이 예약이 된다면 예약 상태가 변경된다`() {
        val seatInventory = SeatInventory(seats = SeatInventory.defaultSeatAvailabilities())
        val targetSeat = Seat(3, RowLabel.A)

        val reservedInventory = seatInventory.reserve(targetSeat)
        val result = seatStatusOf(reservedInventory, targetSeat)

        assertThat(result).isEqualTo(SeatStatus.RESERVED)
    }

    @Test
    fun `한 상영관의 여러 좌석 중 target 좌석이 예약이 안된다면 오류가 발생한다`() {
        val seatInventory = SeatInventory(seats = SeatInventory.defaultSeatAvailabilities())
        val targetSeat = Seat(3, RowLabel.A)

        val reservedInventory = seatInventory.reserve(targetSeat)

        assertThrows(IllegalArgumentException::class.java) {
            reservedInventory.reserve(targetSeat)
        }
    }
}
