package domain.model.cart
import domain.model.screen.Screening

// 예약 완료한 예매 리스트를 누적 저장하는 장바구니 도메인
class Cart(
    items: List<CartItem> = listOf(),
) {
    private val items: MutableList<CartItem> = items.toMutableList()

    // 예약 완료한 예매 리스트 저장
    fun add(item: CartItem) {
        items.add(item)
    }

    // 장바구니 전체 항목을 조회한다.
    fun items(): List<CartItem> = items.toList()

    // 사용자가 새로 고른 상영이 기존 장바구니 상영과 시간이 겹치는지 확인한다.
    fun hasOverlapping(screening: Screening): Boolean =
        items.any { item ->
            item.screening.overlapsWith(screening)
        }
}
