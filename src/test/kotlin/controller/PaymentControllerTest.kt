package controller

import domain.model.cart.CartItem
import domain.model.payment.policy.PaymentMethod
import domain.model.seat.RowLabel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import support.screeningFixture
import support.seatFixture

class PaymentControllerTest {
    private fun item(
        date: LocalDate,
        startTime: LocalTime,
        title: String,
        row: RowLabel,
        column: Int,
    ): CartItem =
        CartItem(
            screening =
                screeningFixture(
                    date = date,
                    startTime = startTime,
                    title = title,
                ),
            seats = listOf(seatFixture(row, column)),
        )

    private fun sampleItemsForPricingScenario(): List<CartItem> =
        listOf(
            item(
                date = LocalDate.of(2026, 4, 6),
                startTime = LocalTime.of(13, 0),
                title = "마더",
                row = RowLabel.A,
                column = 9,
            ),
            item(
                date = LocalDate.of(2026, 4, 7),
                startTime = LocalTime.of(10, 0),
                title = "탑건: 매버릭",
                row = RowLabel.E,
                column = 1,
            ),
            item(
                date = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(16, 0),
                title = "체인소맨",
                row = RowLabel.C,
                column = 8,
            ),
        )

    @Test
    fun `결제는 예매별 할인 후 합산하고 포인트 차감 뒤 카드 할인을 적용한다`() {
        val paymentController = PaymentController()

        val items =
            listOf(
                CartItem(
                    screening =
                        screeningFixture(
                            date = LocalDate.of(2025, 9, 20),
                            startTime = LocalTime.of(13, 0),
                            title = "F1 더 무비",
                        ),
                    seats = listOf(seatFixture(RowLabel.D, 1), seatFixture(RowLabel.D, 2)),
                ),
                CartItem(
                    screening =
                        screeningFixture(
                            date = LocalDate.of(2025, 9, 20),
                            startTime = LocalTime.of(16, 0),
                            title = "토이 스토리",
                        ),
                    seats = listOf(seatFixture(RowLabel.C, 2)),
                ),
                CartItem(
                    screening =
                        screeningFixture(
                            date = LocalDate.of(2025, 9, 20),
                            startTime = LocalTime.of(9, 50),
                            title = "아이언맨",
                        ),
                    seats = listOf(seatFixture(RowLabel.A, 1)),
                ),
            )

        val result =
            paymentController.payAmountApply(
                items = items,
                point = 2_000,
                paymentMethod = PaymentMethod.CARD,
            )

        assertThat(result).isEqualTo(50_065)
    }

    @Test
    fun `현재 샘플 입력 시나리오는 최종 결제 금액 38950원을 반환한다`() {
        val paymentController = PaymentController()
        val items = sampleItemsForPricingScenario()

        val result =
            paymentController.payAmountApply(
                items = items,
                point = 500,
                paymentMethod = PaymentMethod.CARD,
            )

        assertThat(result).isEqualTo(38_950)
    }

    @Test
    fun `카드 대신 현금을 선택하면 현금 할인률로 계산된다`() {
        val paymentController = PaymentController()
        val items = sampleItemsForPricingScenario()

        val result =
            paymentController.payAmountApply(
                items = items,
                point = 500,
                paymentMethod = PaymentMethod.CASH,
            )

        assertThat(result).isEqualTo(40_180)
    }

    @Test
    fun `포인트가 할인 적용 후 합계보다 크면 최종 결제 금액은 0원이다`() {
        val paymentController = PaymentController()

        val items =
            listOf(
                CartItem(
                    screening = screeningFixture(LocalDate.of(2026, 4, 7), LocalTime.of(13, 0), "단일 예매"),
                    seats = listOf(seatFixture(RowLabel.A, 1)),
                ),
            )

        val result =
            paymentController.payAmountApply(
                items = items,
                point = 100_000,
                paymentMethod = PaymentMethod.CARD,
            )

        assertThat(result).isEqualTo(0)
    }
}
