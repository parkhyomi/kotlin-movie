package controller

import domain.model.cart.CartItem

class DefaultReservationSummaryFormatter {
    fun format(item: CartItem): String {
        val seatCodes =
            item.seats.joinToString(", ") { seat ->
                "${seat.row.name}${seat.column}"
            }

        return "- [${item.screening.movie.findMovieTitle()}] ${item.screening.screeningDate} ${item.screening.startTime}  좌석: $seatCodes"
    }
}
