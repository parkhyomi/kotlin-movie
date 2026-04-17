import domain.backend.facade.CinemaController
import domain.backend.factory.CinemaControllerFactory
import view.InputView
import view.OutView

fun cinema(
    inputView: InputView = InputView(),
    outView: OutView = OutView(),
    cinemaController: CinemaController = CinemaControllerFactory.withLocalDatabase(),
) {
    if (!inputView.askStartReservation()) {
        outView.showThankYou()
        return
    }

    while (true) {
        val title =
            inputView.readMovieTitle { movieTitle ->
                cinemaController.findScreeningTitle(movieTitle).isNotEmpty()
            }

        val screeningDate =
            inputView.readScreeningDate { date ->
                cinemaController.findScreenings(title, date).isNotEmpty()
            }
        val screenings = cinemaController.findScreenings(title, screeningDate)

        val selectedScreening =
            inputView.readScreeningWithOverlapCheck(screenings) { screening ->
                cinemaController.hasOverlapping(screening)
            }

        val seatStatuses = cinemaController.findSeatStatuses(title, screeningDate, selectedScreening.startTime)
        outView.showSeatLayout(seatStatuses)

        val seatCodes = inputView.readSeatCodes()
        val item =
            cinemaController.reserve(
                movieTitle = title,
                date = screeningDate,
                startTime = selectedScreening.startTime,
                seatCodes = seatCodes,
            )
        outView.showCartItemAdded(item)

        if (!inputView.askAddMoreReservation()) {
            break
        }
    }

    outView.showCart(cinemaController.reservationItems())

    val point = inputView.readPoint()
    val paymentMethod = inputView.readPaymentMethod()
    val resultPrice = cinemaController.payAmountApply(point = point, paymentMethod = paymentMethod)
    outView.showPriceResult(resultPrice)

    if (!inputView.readContinuePayment()) {
        return
    }

    outView.showReservationCompleted(
        items = cinemaController.reservationItems(),
        resultPrice = resultPrice,
        point = point,
    )
    outView.showThankYou()
}
