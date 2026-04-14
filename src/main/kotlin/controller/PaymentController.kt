package controller

import domain.model.cart.CartItem
import domain.model.payment.PaymentCalculator
import domain.model.payment.policy.PaymentMethod

class PaymentController(
    private val paymentCalculator: PaymentCalculator = PaymentCalculator(),
) {
    fun payAmountApply(
        items: List<CartItem>,
        point: Int,
        paymentMethod: PaymentMethod,
    ): Int =
        paymentCalculator.calculate(
            items = items,
            point = point,
            paymentMethod = paymentMethod,
        )
}
