import controller.PaymentController
import controller.ReservationController
import controller.ScreenController
import domain.model.payment.policy.PaymentMethod
import domain.model.screen.Screening
import java.time.LocalDate
import view.ReservationFormatter
import view.SeatLayoutView

private data class ReservedSelection(
    val screening: Screening,
    val seatCodes: List<String>,
)

fun cinema() {
    val screenController = ScreenController()
    val reservationController = ReservationController()
    val paymentController = PaymentController()

    println("영화 예매를 시작합니다. 새 예매를 생성하시겠습니까? (Y/N)")
    if (!readYesNo()) {
        println("감사합니다.")
        return
    }

    while (true) {
        // 영화 제목을 입력 받고 해당 제목 영화의 상영 목록
        val title = readMovieTitle(screenController)

        // 영화 날짜를 입력 받고 제목으로 필터링 된 리스트 중 해당 날짜 리스트 반환
        val screeningDate = readScreeningDate(screenController, title)
        val screenings = screenController.findScreeningsDate(screenController.findScreeningTitle(title), screeningDate)

        // 상영 시간을 선택한다. 장바구니 내 기존 예약과 시간이 겹치면 재선택한다.
        val selectedScreening = readScreeningWithOverlapCheck(screenings, reservationController)

        // 사용자가 고른 상영의 좌석 배치도를 출력한다.
        val seatStatuses = screenController.findSeatStatuses(title, screeningDate, selectedScreening.startTime)
        println("좌석 배치도")
        SeatLayoutView.render(seatStatuses)

        // 좌석 입력을 받아 실제 예약한다.
        val reserved = reserveSeatsForSelection(screenController, selectedScreening, title, screeningDate)
        val item = reservationController.reserve(reserved.screening, reserved.seatCodes)

        println()
        println("장바구니에 추가됨")
        println(ReservationFormatter.format(item))
        println()

        println("다른 영화를 추가하시겠습니까? (Y/N)")
        if (!readYesNo()) {
            break
        }
    }

    println("장바구니")
    reservationController
        .reservationSummaries()
        .forEach { summary ->
            println(summary)
        }

    println("사용할 포인트를 입력하세요 (없으면 0):")
    val point = readln().trim().toIntOrNull() ?: 0

    println("결제 수단을 선택하세요:")
    println("1. 신용카드")
    println("2. 현금")
    val paymentMethod = PaymentMethod.entries[readln().trim().toInt() - 1]
    paymentController.usePoint(point)
    paymentController.selectPaymentMethod(paymentMethod)

    println("가격계산")
    val resultPrice = paymentController.payAmountApply(reservationController.reservationItems())
    println("최종 결제 금액: $resultPrice")

    println("위 금액으로 결제하시겠습니까? (Y/N)")
    if (!readContinuePayment()) {
        return
    }

    println("예매 완료")
    reservationController
        .reservationSummaries()
        .forEach { summary ->
            println(summary)
        }

    println("결제 금액: $resultPrice  (포인트 ${point}원 사용)")

    println("감사합니다")
}

// Y/N 값을 검증해서 반환한다.
private fun readYesNo(): Boolean {
    while (true) {
        val answer = readln().trim().uppercase()

        if (answer == "Y") {
            return true
        }
        if (answer == "N") {
            return false
        }

        println("Y 또는 N을 입력해 주세요.")
    }
}

// 존재하는 상영 제목을 입력 받을 때까지 반복한다.
private fun readMovieTitle(screenController: ScreenController): String {
    while (true) {
        println("예매할 영화 제목을 입력하세요:")
        val title = readln().trim()

        if (title.isBlank()) {
            println("영화 제목은 비어 있을 수 없습니다.")
            continue
        }

        if (screenController.findScreeningTitle(title).isEmpty()) {
            println("해당 제목의 상영이 없습니다. 다시 입력해 주세요.")
            continue
        }

        return title
    }
}

// 해당 영화에 상영이 있는 날짜를 입력 받을 때까지 반복한다.
private fun readScreeningDate(
    screenController: ScreenController,
    movieTitle: String,
): LocalDate {
    while (true) {
        println("날짜를 입력하세요 (YYYY-MM-DD):")
        val input = readln().trim()
        val date = runCatching { LocalDate.parse(input) }.getOrNull()

        if (date == null) {
            println("날짜 형식이 올바르지 않습니다.")
            continue
        }

        val screenings = screenController.findScreeningsDate(screenController.findScreeningTitle(movieTitle), date)
        if (screenings.isEmpty()) {
            println("해당 날짜에는 상영이 없습니다. 다른 날짜를 입력해 주세요.")
            continue
        }

        return date
    }
}

// 상영 번호를 선택한다. 장바구니 내 예약과 시간이 겹치면 재선택한다.
private fun readScreeningWithOverlapCheck(
    screenings: List<Screening>,
    reservationController: ReservationController,
): Screening {
    val ordered = screenings.sortedBy { screening -> screening.startTime }

    while (true) {
        println("해당 날짜의 상영 목록")
        ordered.forEachIndexed { index, screening ->
            println("[${index + 1}] ${screening.startTime}")
        }

        val selectedIndex = readScreeningIndex(ordered.size)
        val selectedScreening = ordered[selectedIndex - 1]

        if (reservationController.hasOverlapping(selectedScreening)) {
            println("선택하신 상영 시간이 겹칩니다. 다른 시간을 선택해 주세요.")
            continue
        }

        return selectedScreening
    }
}

// 상영 목록에서 유효한 번호를 선택할 때까지 반복한다.
private fun readScreeningIndex(maxIndex: Int): Int {
    while (true) {
        println("상영 번호를 선택하세요:")
        val index = readln().trim().toIntOrNull()

        if (index == null) {
            println("숫자 번호를 입력해 주세요.")
            continue
        }

        if (index !in 1..maxIndex) {
            println("존재하지 않는 상영 번호입니다.")
            continue
        }

        return index
    }
}

// 좌석 입력을 받아 선택한 상영에 예약을 반영한다.
private fun reserveSeatsForSelection(
    screenController: ScreenController,
    screening: Screening,
    movieTitle: String,
    screeningDate: LocalDate,
): ReservedSelection {
    while (true) {
        println("예약할 좌석을 입력하세요 (A1, B2):")
        val seatCodes = readSeatCodes()

        val result =
            screenController.reserveSeats(
                movieTitle = movieTitle,
                date = screeningDate,
                startTime = screening.startTime,
                seats = seatCodes,
            )
        return ReservedSelection(result, seatCodes)
    }
}

// 좌석 코드를 리스트로 파싱한다.
private fun readSeatCodes(): List<String> {
    while (true) {
        val seats =
            readln()
                .split(",")
                .map { code -> code.trim() }
                .filter { code -> code.isNotBlank() }

        if (seats.isEmpty()) {
            println("좌석을 1개 이상 입력해 주세요.")
            continue
        }

        return seats
    }
}

// 결제 진행 여부(Y/N)를 입력받는다.
private fun readContinuePayment(): Boolean {
    while (true) {
        val answer = readln().trim().uppercase()

        if (answer == "Y") {
            return true
        }
        if (answer == "N") {
            return false
        }
    }
}
