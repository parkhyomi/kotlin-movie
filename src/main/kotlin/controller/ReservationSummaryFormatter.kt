package controller

import domain.model.cart.CartItem
import view.ReservationFormatter

interface ReservationSummaryFormatter {
    fun format(item: CartItem): String
}

class DefaultReservationSummaryFormatter : ReservationSummaryFormatter {
    override fun format(item: CartItem): String = ReservationFormatter.format(item)
}
