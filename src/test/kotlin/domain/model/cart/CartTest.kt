package domain.model.cart

import domain.model.seat.RowLabel
import domain.model.seat.Seat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import support.screeningFixture

class CartTest {
    @Test
    fun `장바구니에 항목을 추가하면 items로 조회할 수 있다`() {
        val cart = Cart()
        val item =
            CartItem(
                screening = screeningFixture(),
                seats =
                    listOf(
                        Seat(column = 2, row = RowLabel.C),
                        Seat(column = 3, row = RowLabel.C),
                    ),
            )

        cart.add(item)

        assertThat(cart.items()).containsExactly(item)
    }

    @Test
    fun `기존 장바구니 상영과 시간이 겹치면 hasOverlapping은 true를 반환한다`() {
        val cart = Cart()
        val reserved =
            CartItem(
                screening =
                    screeningFixture(
                        date = LocalDate.of(2026, 4, 10),
                        startTime = LocalTime.of(10, 0),
                        runningMinutes = 120,
                    ),
                seats = listOf(Seat(column = 1, row = RowLabel.A)),
            )
        cart.add(reserved)

        val candidate =
            screeningFixture(
                date = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(11, 0),
                runningMinutes = 90,
            )

        val result = cart.hasOverlapping(candidate)

        assertThat(result).isTrue()
    }

    @Test
    fun `기존 장바구니 상영과 시간이 겹치지 않으면 hasOverlapping은 false를 반환한다`() {
        val cart = Cart()
        val reserved =
            CartItem(
                screening =
                    screeningFixture(
                        date = LocalDate.of(2026, 4, 10),
                        startTime = LocalTime.of(10, 0),
                        runningMinutes = 120,
                    ),
                seats = listOf(Seat(column = 1, row = RowLabel.A)),
            )
        cart.add(reserved)

        val candidate =
            screeningFixture(
                date = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(12, 0),
                runningMinutes = 90,
            )

        val result = cart.hasOverlapping(candidate)

        assertThat(result).isFalse()
    }

    @Test
    fun `기존 장바구니 상영과 시간이 겹치면 add에서 예외가 발생한다`() {
        val cart = Cart()
        val first =
            CartItem(
                screening =
                    screeningFixture(
                        date = LocalDate.of(2026, 4, 10),
                        startTime = LocalTime.of(10, 0),
                        runningMinutes = 120,
                    ),
                seats = listOf(Seat(column = 1, row = RowLabel.A)),
            )
        val overlapped =
            CartItem(
                screening =
                    screeningFixture(
                        date = LocalDate.of(2026, 4, 10),
                        startTime = LocalTime.of(11, 0),
                        runningMinutes = 90,
                    ),
                seats = listOf(Seat(column = 2, row = RowLabel.A)),
            )
        cart.add(first)

        assertThrows(IllegalArgumentException::class.java) {
            cart.add(overlapped)
        }
    }
}
