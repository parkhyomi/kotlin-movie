package domain.model.payment.policy

import domain.model.ScreeningSchedule.Screening

interface DiscountPolicy {
    fun apply(
        amount: Int,
        screening: Screening,
    ): Int
}
