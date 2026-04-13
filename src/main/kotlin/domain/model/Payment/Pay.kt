package domain.model.payment

data class Pay(
    private val point: Int,
    private val paymentMethod: PaymentMethod,
) {
    init {
        require(point >= 0) { "포인트는 0 이상이어야 합니다." }
    }

    fun payAmountApply(payAmount: Int): Int {
        val pointAppliedAmount = applyPoint(payAmount).coerceAtLeast(0)
        val discountAmount = paymentMethodDiscountAmount(pointAppliedAmount)

        return (pointAppliedAmount - discountAmount).coerceAtLeast(0)
    }

    private fun applyPoint(payAmount: Int): Int = payAmount - point

    private fun paymentMethodDiscountAmount(payAmount: Int): Int = (payAmount * paymentMethod.discountRate).toInt()
}

enum class PaymentMethod(
    val discountRate: Double,
) {
    CARD(0.05),
    CASH(0.02),
}
