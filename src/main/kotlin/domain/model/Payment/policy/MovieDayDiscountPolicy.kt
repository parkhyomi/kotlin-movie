package domain.model.payment.policy

import domain.model.ScreeningSchedule.Screening

class MovieDayDiscountPolicy(
    private val movieDays: Set<Int> = setOf(10, 20, 30),
    private val discountPercent: Int = 10,
) : DiscountPolicy {
    override fun apply(
        amount: Int,
        screening: Screening,
    ): Int {
        if (screening.screeningDate.dayOfMonth !in movieDays) {
            return amount
        }

        return amount - (amount * discountPercent / 100)
    }
}
