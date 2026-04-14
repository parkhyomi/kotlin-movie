package domain.model.payment

import domain.model.payment.policy.PayMethodPolicy
import domain.model.payment.policy.PaymentMethod

data class Pay(
    private val point: Int,
    private val paymentMethod: PaymentMethod,
) {
    init {
        require(point >= 0) { "포인트는 0 이상이어야 합니다." }
    }

    private val payMethodPolicy = PayMethodPolicy()

    fun payAmountApply(payAmount: Int): Int {
        val amount = payAmount - point
        return payMethodPolicy.payAmountApply(amount, paymentMethod).coerceAtLeast(0)
    }

}