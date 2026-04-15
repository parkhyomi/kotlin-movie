package domain.model.payment

import domain.model.payment.policy.PayMethodPolicy
import domain.model.payment.policy.PaymentMethod

data class Pay(
    private val point: Point,
    private val paymentMethod: PaymentMethod,
) {
    constructor(
        point: Int,
        paymentMethod: PaymentMethod,
    ) : this(
        point = Point.from(point),
        paymentMethod = paymentMethod,
    )

    private val payMethodPolicy = PayMethodPolicy()

    fun payAmountApply(payAmount: Int): Int {
        val amount = payAmount - point.value
        return payMethodPolicy.payAmountApply(amount, paymentMethod).coerceAtLeast(0)
    }

}