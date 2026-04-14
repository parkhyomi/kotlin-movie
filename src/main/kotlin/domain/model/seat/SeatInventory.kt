package domain.model.seat

import domain.model.seat.policy.DefaultSeatReservationPolicy
import domain.model.seat.policy.SeatReservationPolicy

data class SeatInventory(
    private val seats: List<SeatAvailability>,
    private val seatReservationPolicy: SeatReservationPolicy = DefaultSeatReservationPolicy(),
) {

    fun reserve(targetSeat: Seat): SeatInventory {
        require(seatReservationPolicy.canReserve(seats, targetSeat)) { "이미 예약된 좌석입니다." }
        return copy(
            seats =
                seats.map { seatAvailability ->
                    if (!seatAvailability.isSeat(targetSeat)) {
                        return@map seatAvailability
                    }
                    seatAvailability.reserve()
                },
        )
    }

    fun statuses(): List<SeatAvailability> = seats

    companion object {
        fun defaultSeatAvailabilities(): List<SeatAvailability> =
            Seat.columns.flatMap { column ->
                Seat.rows.map { row ->
                    SeatAvailability(
                        seat =
                            Seat(
                                column = column,
                                row = row,
                            ),
                    )
                }
            }
    }
}
