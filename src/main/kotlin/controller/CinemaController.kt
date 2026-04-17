package controller

import domain.model.cart.Cart
import domain.model.cart.CartItem
import domain.model.payment.PaymentCalculator
import domain.model.payment.policy.PaymentMethod
import domain.model.screeningschedule.Screening
import domain.model.seat.SeatAvailability
import infra.db.repository.ScreeningRepository
import java.time.LocalDate
import java.time.LocalTime

class CinemaController(
    private val screeningRepository: ScreeningRepository,
    private val paymentCalculator: PaymentCalculator = PaymentCalculator(),
    private val cart: Cart = Cart(),
    private val seatCodeParser: DefaultSeatCodeParser = DefaultSeatCodeParser(),
) {
    fun findScreeningTitle(title: String): List<Screening> = screeningRepository.screeningsOfMovieTitle(title)

    fun findScreenings(
        title: String,
        date: LocalDate,
    ): List<Screening> = screeningRepository.screeningsOf(title, date)

    fun findSeatStatuses(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability> = screeningRepository.seatStatusesOf(movieTitle, date, startTime)

    fun reserve(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seatCodes: List<String>,
    ): CartItem {
        val selectedScreening = screeningOf(movieTitle, date, startTime)
        require(!cart.hasOverlapping(selectedScreening)) { "선택하신 상영 시간이 겹칩니다." }

        val seats = seatCodeParser.parse(seatCodes)
        val reservedScreening =
            screeningRepository.reserveSeats(
                movieTitle = movieTitle,
                date = date,
                startTime = startTime,
                seats = seats,
            )

        val item = CartItem(screening = reservedScreening, seats = seats)
        cart.add(item)
        return item
    }

    fun hasOverlapping(screening: Screening): Boolean = cart.hasOverlapping(screening)

    fun reservationItems(): List<CartItem> = cart.items()

    fun payAmountApply(
        point: Int,
        paymentMethod: PaymentMethod,
    ): Int =
        paymentCalculator.calculate(
            items = cart.items(),
            point = point,
            paymentMethod = paymentMethod,
        )

    private fun screeningOf(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): Screening =
        findScreenings(movieTitle, date)
            .firstOrNull { screening -> screening.startsAt(startTime) }
            ?: throw IllegalArgumentException("해당 조건의 상영이 존재하지 않습니다.")
}
