package domain.model.seat.policy

import domain.model.seat.Seat
import domain.model.seat.SeatAvailability

class DefaultSeatReservationPolicy : SeatReservationPolicy {
    override fun canReserve(
        seats: List<SeatAvailability>,
        targetSeat: Seat,
    ): Boolean =
        seats.any { seatAvailability ->
            seatAvailability.isSeat(targetSeat) && seatAvailability.isAvailable()
        }
}
