package view

import domain.model.cart.CartItem

object ReservationFormatter {
    fun format(item: CartItem): String {
        val seatCodes =
            item.seats.joinToString(", ") { seat ->
                "${seat.row.name}${seat.column}"
            }

        return "- [${item.screening.movie.title}] ${item.screening.screeningDate} ${item.screening.startTime}  좌석: $seatCodes"
    }
}
