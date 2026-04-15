package domain.model.payment.policy

import domain.model.screeningschedule.Screening

interface DiscountPolicy {
    fun apply(
        amount: Int,
        screening: Screening,
    ): Int
}
