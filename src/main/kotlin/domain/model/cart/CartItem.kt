package domain.model.cart

import domain.model.screeningschedule.Screening
import domain.model.seat.Seat

data class CartItem(
    val screening: Screening,
    val seats: List<Seat>,
) {
    fun seatAmount(): Int =
        seats.sumOf { seat ->
            seat.seatClass.price
        }
}
