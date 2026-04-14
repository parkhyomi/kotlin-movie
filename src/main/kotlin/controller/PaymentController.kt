package controller

import domain.model.cart.CartItem
import domain.model.payment.PaymentCalculator
import domain.model.payment.policy.PaymentMethod

class PaymentController(
    private var point: Int = 0,
    private var paymentMethod: PaymentMethod = PaymentMethod.CARD,
    private val paymentCalculator: PaymentCalculator = PaymentCalculator(),
) {

    fun usePoint(point: Int) {
        require(point >= 0) { "포인트는 0 이상이어야 합니다." }
        this.point = point
    }

    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        this.paymentMethod = paymentMethod
    }

    fun payAmountApply(items: List<CartItem>): Int =
        paymentCalculator.calculate(
            items = items,
            point = point,
            paymentMethod = paymentMethod,
        )
}
