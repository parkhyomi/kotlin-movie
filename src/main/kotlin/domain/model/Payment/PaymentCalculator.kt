package domain.model.payment

import domain.model.cart.CartItem
import domain.model.payment.policy.PaymentMethod

class PaymentCalculator(
    private val discount: Discount = Discount(),
) {
    fun calculate(
        items: List<CartItem>,
        point: Int,
        paymentMethod: PaymentMethod,
    ): Int {
        val discountAppliedAmount =
            items.sumOf { item ->
                discount.discountAmountApply(
                    amount = item.seatAmount(),
                    screening = item.screening,
                )
            }

        return Pay(
            point = point,
            paymentMethod = paymentMethod,
        ).payAmountApply(discountAppliedAmount)
    }
}
