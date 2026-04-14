package controller

import domain.model.cart.Cart
import domain.model.cart.CartItem
import domain.model.ScreeningSchedule.Screening

class ReservationController(
    private val cart: Cart = Cart(),
    private val seatCodeParser: SeatCodeParser = DefaultSeatCodeParser(),
    private val reservationSummaryFormatter: ReservationSummaryFormatter = DefaultReservationSummaryFormatter(),
) {
    // 예약 저장하기
    fun reserve(
        screening: Screening,
        seatCodes: List<String>,
    ): CartItem {
        val item =
            CartItem(
                screening = screening,
                seats = seatCodeParser.parse(seatCodes),
            )
        cart.add(item)
        return item
    }

    // 예약 리스트별 제목, 날짜, 시작시간, 좌석 추출하기
    fun reservationSummaries(): List<String> =
        cart.items().map { item ->
            reservationSummaryFormatter.format(item)
        }

    // 사용자가 고른 상영 시간이 기존 장바구니와 겹치는지 확인한다.
    fun hasOverlapping(screening: Screening): Boolean = cart.hasOverlapping(screening)

    // 장바구니 항목 원본 목록을 조회한다.
    fun reservationItems(): List<CartItem> = cart.items()
}
