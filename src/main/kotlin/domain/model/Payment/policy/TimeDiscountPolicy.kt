package domain.model.payment.policy

import domain.model.ScreeningSchedule.Screening
import java.time.LocalTime

class TimeDiscountPolicy(
    private val morningCutoff: LocalTime = LocalTime.of(11, 0),
    private val nightCutoff: LocalTime = LocalTime.of(20, 0),
    private val discountAmount: Int = 2_000,
) : DiscountPolicy {
    override fun apply(
        amount: Int,
        screening: Screening,
    ): Int {
        val isDiscountTime = screening.startTime !in morningCutoff..nightCutoff
        if (!isDiscountTime) {
            return amount
        }

        return amount - discountAmount
    }
}
