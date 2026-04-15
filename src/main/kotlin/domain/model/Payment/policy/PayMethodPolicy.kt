package domain.model.payment.policy

class PayMethodPolicy {
    fun payAmountApply(
        amount: Int,
        paymentMethod: PaymentMethod,
    ): Int = amount - (amount * paymentMethod.discountRate).toInt()
}

enum class PaymentMethod(
    val discountRate: Double,
) {
    CARD(0.05),
    CASH(0.02),
    ;

    init {
        require(discountRate in 0.0..1.0) { "결제 수단 할인율은 0.0 이상 1.0 이하여야 합니다." }
    }
}
