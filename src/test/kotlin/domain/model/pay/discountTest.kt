package domain.model.pay

import domain.model.payment.Discount
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import support.screeningFixture

class DiscountTest {
    private fun screening(
        day: Int,
        startTime: LocalTime,
    ) = screeningFixture(
        date = LocalDate.of(2026, 4, day),
        startTime = startTime,
    )

    @Test
    fun `무비데이만 적용되면 10퍼센트 할인된다`() {
        val discount =
            Discount(
                screening =
                    screening(
                        day = 10,
                        startTime = LocalTime.of(13, 0),
                    ),
            )

        val result = discount.discountAmountApply(discountAmount = 10_000)

        assertThat(result).isEqualTo(9_000)
    }

    @Test
    fun `오전 11시 이전 시작 상영은 2000원 시간 할인이 적용된다`() {
        val discount =
            Discount(
                screening =
                    screening(
                        day = 9,
                        startTime = LocalTime.of(10, 59),
                    ),
            )

        val result = discount.discountAmountApply(discountAmount = 10_000)

        assertThat(result).isEqualTo(8_000)
    }

    @Test
    fun `오후 8시 이후 시작 상영은 2000원 시간 할인이 적용된다`() {
        val discount =
            Discount(
                screening =
                    screening(
                        day = 9,
                        startTime = LocalTime.of(20, 10),
                    ),
            )

        val result = discount.discountAmountApply(discountAmount = 10_000)

        assertThat(result).isEqualTo(8_000)
    }

    @Test
    fun `무비데이와 시간 할인이 동시에 적용되면 비율할인 후 정액할인이 적용된다`() {
        val discount =
            Discount(
                screening =
                    screening(
                        day = 20,
                        startTime = LocalTime.of(10, 0),
                    ),
            )

        val result = discount.discountAmountApply(discountAmount = 10_000)

        assertThat(result).isEqualTo(7_000)
    }

    @Test
    fun `할인 조건이 없으면 금액은 그대로 유지된다`() {
        val discount =
            Discount(
                screening =
                    screening(
                        day = 9,
                        startTime = LocalTime.of(13, 0),
                    ),
            )

        val result = discount.discountAmountApply(discountAmount = 10_000)

        assertThat(result).isEqualTo(10_000)
    }

    @Test
    fun `오전 11시 정각과 오후 8시 정각은 시간 할인 조건에 해당하지 않는다`() {
        val morning =
            Discount(
                screening =
                    screening(
                        day = 9,
                        startTime = LocalTime.of(11, 0),
                    ),
            )
        val night =
            Discount(
                screening =
                    screening(
                        day = 9,
                        startTime = LocalTime.of(20, 0),
                    ),
            )

        val morningResult = morning.discountAmountApply(discountAmount = 10_000)
        val nightResult = night.discountAmountApply(discountAmount = 10_000)

        assertThat(morningResult).isEqualTo(10_000)
        assertThat(nightResult).isEqualTo(10_000)
    }

    @Test
    fun `할인 결과가 음수가 되면 0원으로 보정된다`() {
        val discount =
            Discount(
                screening =
                    screening(
                        day = 30,
                        startTime = LocalTime.of(10, 0),
                    ),
            )

        val result = discount.discountAmountApply(discountAmount = 1_000)

        assertThat(result).isEqualTo(0)
    }
}
