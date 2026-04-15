import controller.PaymentController
import controller.ReservationController
import controller.ScreenController
import view.InputView
import view.OutView

fun cinema(
    inputView: InputView = InputView(),
    outView: OutView = OutView(),
) {
    val screenController = ScreenController()
    val reservationController = ReservationController()
    val paymentController = PaymentController()

    if (!inputView.askStartReservation()) {
        outView.showThankYou()
        return
    }

    while (true) {
        val title =
            inputView.readMovieTitle { movieTitle ->
                screenController.findScreeningTitle(movieTitle).isNotEmpty()
            }

        val screeningDate =
            inputView.readScreeningDate { date ->
                screenController.findScreenings(title, date).isNotEmpty()
            }
        val screenings = screenController.findScreenings(title, screeningDate)

        val selectedScreening =
            inputView.readScreeningWithOverlapCheck(screenings) { screening ->
                reservationController.hasOverlapping(screening)
            }

        val seatStatuses = screenController.findSeatStatuses(title, screeningDate, selectedScreening.startTime)
        outView.showSeatLayout(seatStatuses)

        val seatCodes = inputView.readSeatCodes()
        val reservedScreening =
            screenController.reserveSeats(
                movieTitle = title,
                date = screeningDate,
                startTime = selectedScreening.startTime,
                seats = seatCodes,
            )
        val item = reservationController.reserve(reservedScreening, seatCodes)
        outView.showCartItemAdded(item)

        if (!inputView.askAddMoreReservation()) {
            break
        }
    }

    outView.showCart(reservationController.reservationSummaries())

    val point = inputView.readPoint()
    val paymentMethod = inputView.readPaymentMethod()
    val resultPrice =
        paymentController.payAmountApply(
            items = reservationController.reservationItems(),
            point = point,
            paymentMethod = paymentMethod,
        )
    outView.showPriceResult(resultPrice)

    if (!inputView.readContinuePayment()) {
        return
    }

    outView.showReservationCompleted(
        summaries = reservationController.reservationSummaries(),
        resultPrice = resultPrice,
        point = point,
    )
    outView.showThankYou()
}
