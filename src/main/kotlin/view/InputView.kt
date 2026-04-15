package view

import domain.model.ScreeningSchedule.Screening
import domain.model.payment.policy.PaymentMethod
import java.time.LocalDate

class InputView {
    fun askStartReservation(): Boolean {
        println("영화 예매를 시작합니다. 새 예매를 생성하시겠습니까? (Y/N)")
        return readYesNoWithGuide()
    }

    fun askAddMoreReservation(): Boolean {
        println("다른 영화를 추가하시겠습니까? (Y/N)")
        return readYesNoWithGuide()
    }

    fun readMovieTitle(isAvailableTitle: (String) -> Boolean): String {
        while (true) {
            println("예매할 영화 제목을 입력하세요:")
            val title = readln().trim()

            if (title.isBlank()) {
                println("영화 제목은 비어 있을 수 없습니다.")
                continue
            }

            if (!isAvailableTitle(title)) {
                println("해당 제목의 상영이 없습니다. 다시 입력해 주세요.")
                continue
            }

            return title
        }
    }

    fun readScreeningDate(isAvailableDate: (LocalDate) -> Boolean): LocalDate {
        while (true) {
            println("날짜를 입력하세요 (YYYY-MM-DD):")
            val input = readln().trim()
            val date = runCatching { LocalDate.parse(input) }.getOrNull()

            if (date == null) {
                println("날짜 형식이 올바르지 않습니다.")
                continue
            }

            if (!isAvailableDate(date)) {
                println("해당 날짜에는 상영이 없습니다. 다른 날짜를 입력해 주세요.")
                continue
            }

            return date
        }
    }

    fun readScreeningWithOverlapCheck(
        screenings: List<Screening>,
        hasOverlapping: (Screening) -> Boolean,
    ): Screening {
        val ordered = screenings.sortedBy { screening -> screening.startTime }

        while (true) {
            println("해당 날짜의 상영 목록")
            ordered.forEachIndexed { index, screening ->
                println("[${index + 1}] ${screening.startTime}")
            }

            val selectedIndex = readScreeningIndex(ordered.size)
            val selectedScreening = ordered[selectedIndex - 1]

            if (hasOverlapping(selectedScreening)) {
                println("선택하신 상영 시간이 겹칩니다. 다른 시간을 선택해 주세요.")
                continue
            }

            return selectedScreening
        }
    }

    fun readSeatCodes(): List<String> {
        while (true) {
            println("예약할 좌석을 입력하세요 (A1, B2):")
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

    fun readPoint(): Int {
        println("사용할 포인트를 입력하세요 (없으면 0):")
        return readln().trim().toIntOrNull() ?: 0
    }

    fun readPaymentMethod(): PaymentMethod {
        println("결제 수단을 선택하세요:")
        println("1. 신용카드")
        println("2. 현금")
        return PaymentMethod.entries[readln().trim().toInt() - 1]
    }

    fun readContinuePayment(): Boolean {
        println("위 금액으로 결제하시겠습니까? (Y/N)")
        return readYesNoWithoutGuide()
    }

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

    private fun readYesNoWithGuide(): Boolean {
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

    private fun readYesNoWithoutGuide(): Boolean {
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
}
