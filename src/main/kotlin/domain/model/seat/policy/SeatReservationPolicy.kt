package domain.model.seat.policy

import domain.model.seat.Seat
import domain.model.seat.SeatAvailability

interface SeatReservationPolicy {
    fun canReserve(
        seats: List<SeatAvailability>,
        targetSeat: Seat,
    ): Boolean
}