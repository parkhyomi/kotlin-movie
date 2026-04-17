package view

import domain.model.cart.CartItem
import domain.model.seat.SeatAvailability

class OutView {
    fun showThankYou() {
        println("감사합니다")
    }

    fun showSeatLayout(seatStatuses: List<SeatAvailability>) {
        println("좌석 배치도")
        SeatLayoutView.render(seatStatuses)
    }

    fun showCartItemAdded(item: CartItem) {
        println()
        println("장바구니에 추가됨")
        println(ReservationFormatter.format(item))
        println()
    }

    fun showCart(items: List<CartItem>) {
        println("장바구니")
        items.forEach { item ->
            println(ReservationFormatter.format(item))
        }
    }

    fun showPriceResult(resultPrice: Int) {
        println("가격계산")
        println("최종 결제 금액: $resultPrice")
    }

    fun showReservationCompleted(
        items: List<CartItem>,
        resultPrice: Int,
        point: Int,
    ) {
        println("예매 완료")
        items.forEach { item ->
            println(ReservationFormatter.format(item))
        }
        println("결제 금액: $resultPrice  (포인트 ${point}원 사용)")
    }
}
