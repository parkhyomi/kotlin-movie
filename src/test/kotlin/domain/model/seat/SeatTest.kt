package domain.model.seat

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SeatTest {
    @Test
    fun `좌석 행 열을 입력하였을때 해당 좌석이 A,B는 B등급으로 표시된다 `() {
        val seats = listOf(Seat(3, RowLabel.A), Seat(1, RowLabel.B))

        seats.forEach { seat ->
            assertThat(seat.seatClass).isEqualTo(SeatClass.B)
        }
    }

    @Test
    fun `좌석 행 열을 입력하였을때 해당 좌석이 C라면 A등급으로 표시된다 `() {
        val seat = Seat(3, RowLabel.C)

        assertThat(seat.seatClass).isEqualTo(SeatClass.A)
    }

    @Test
    fun `좌석 행 열을 입력하였을때 해당 좌석이 D,E라면 S등급으로 표시된다 `() {
        val seats = listOf(Seat(3, RowLabel.D), Seat(1, RowLabel.E))

        seats.forEach { seat ->
            assertThat(seat.seatClass).isEqualTo(SeatClass.S)
        }
    }
}
