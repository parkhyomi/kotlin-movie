package domain.model.payment

import domain.model.payment.policy.DiscountPolicy
import domain.model.payment.policy.MovieDayDiscountPolicy
import domain.model.payment.policy.TimeDiscountPolicy
import domain.model.screeningschedule.Screening

class Discount {
    private val policies: List<DiscountPolicy> =
        listOf(
            MovieDayDiscountPolicy(),
            TimeDiscountPolicy(),
        )

    fun discountAmountApply(
        amount: Int,
        screening: Screening,
    ): Int = normalizeNonNegative(applyPolicies(amount, screening))

    private fun applyPolicies(
        amount: Int,
        screening: Screening,
    ): Int =
        policies.fold(amount) { currentAmount, policy ->
            policy.apply(currentAmount, screening)
        }

    private fun normalizeNonNegative(amount: Int): Int = amount.coerceAtLeast(0)
}
