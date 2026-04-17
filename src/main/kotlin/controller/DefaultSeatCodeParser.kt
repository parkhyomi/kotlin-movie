package controller

import domain.model.seat.RowLabel
import domain.model.seat.Seat

class DefaultSeatCodeParser {
    fun parse(seatCodes: List<String>): List<Seat> =
        seatCodes
            .filter { seatCode -> seatCode.isNotBlank() }
            .map { seatCode -> parseSeatCode(seatCode) }

    private fun parseSeatCode(code: String): Seat {
        val value = code.trim().uppercase()
        require(value.isNotBlank()) { "좌석 값이 비어 있습니다." }

        val rowPart = value.takeWhile { token -> token.isLetter() }
        val columnPart = value.dropWhile { token -> token.isLetter() }

        require(rowPart.length == 1) { "좌석 행은 알파벳 1글자여야 합니다: $code" }
        require(columnPart.isNotEmpty()) { "좌석 번호가 없습니다: $code" }
        require(columnPart.all { token -> token.isDigit() }) { "좌석 번호는 숫자여야 합니다: $code" }

        return Seat(
            column = columnPart.toInt(),
            row = RowLabel.valueOf(rowPart),
        )
    }
}
