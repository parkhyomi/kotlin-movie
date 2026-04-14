package domain.model.payment.policy

import domain.model.screen.Screening

interface DiscountPolicy {
    fun apply(
        amount: Int,
        screening: Screening,
    ): Int
}
