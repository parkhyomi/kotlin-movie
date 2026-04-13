package domain.model.payment

import domain.model.payment.policy.MovieDayDiscountPolicy
import domain.model.payment.policy.TimeDiscountPolicy
import domain.model.screen.Screening

data class Discount(
    private val screening: Screening? = null,
) {
    private val movieDayDiscountPolicy: MovieDayDiscountPolicy = MovieDayDiscountPolicy()
    private val timeDiscountPolicy: TimeDiscountPolicy = TimeDiscountPolicy()

    fun discountAmountApply(discountAmount: Int): Int {
        val source = screening ?: return discountAmount
        val movieDayDiscountApplied = movieDayDiscountPolicy.apply(discountAmount, source)

        return timeDiscountPolicy
            .apply(movieDayDiscountApplied, source)
            .coerceAtLeast(0)
    }
}
