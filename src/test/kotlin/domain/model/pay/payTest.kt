package domain.model.pay

import domain.model.payment.Pay
import domain.model.payment.policy.PaymentMethod
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PayTest {
    @Test
    fun `포인가 있을경우 포인트가 먼저 차감되고 차감된 가격에서 결제수단에 맞춰 할인률이 적용된다`() {
        val pay =
            Pay(
                point = 2000,
                paymentMethod = PaymentMethod.CARD,
            )

        val result = pay.payAmountApply(10000)

        assertThat(result).isEqualTo(7600)
    }

    @Test
    fun `포인트가 0이면 전체 금액에서 결제수단 할인만 적용된다`() {
        val pay =
            Pay(
                point = 0,
                paymentMethod = PaymentMethod.CASH,
            )

        val result = pay.payAmountApply(10000)

        assertThat(result).isEqualTo(9800)
    }

    @Test
    fun `포인트가 결제 금액보다 크면 최종 결제 금액은 0원이다`() {
        val pay =
            Pay(
                point = 15000,
                paymentMethod = PaymentMethod.CARD,
            )

        val result = pay.payAmountApply(10000)

        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `포인트가 음수이면 Pay를 생성할 수 없다`() {
        assertThrows(IllegalArgumentException::class.java) {
            Pay(
                point = -1,
                paymentMethod = PaymentMethod.CARD,
            )
        }
    }
}
