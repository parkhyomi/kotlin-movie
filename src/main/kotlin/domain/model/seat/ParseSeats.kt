package domain.model.seat

// 입력받은 좌석 문자열 목록(A1, B12...)을 Seat 목록으로 변환한다.
fun parseSeats(seatCodes: List<String>): List<Seat> =
    seatCodes
        .filter { seatCode ->
            seatCode.isNotBlank()
        }.map { seatCode ->
            parseSeatCode(seatCode)
        }

// 좌석 코드 하나를 파싱한다. (알파벳 1글자 + 숫자 1자리 이상)
private fun parseSeatCode(code: String): Seat {
    val value = code.trim().uppercase()
    require(value.isNotBlank()) { "좌석 값이 비어 있습니다." }

    val rowPart = value.takeWhile { token -> token.isLetter() }
    val columnPart = value.dropWhile { token -> token.isLetter() }

    require(rowPart.length == 1) { "좌석 행은 알파벳 1글자여야 합니다: $code" }
    require(columnPart.isNotEmpty()) { "좌석 번호가 없습니다: $code" }
    require(columnPart.all { token -> token.isDigit() }) { "좌석 번호는 숫자여야 합니다: $code" }

    val row = RowLabel.valueOf(rowPart)
    val column = columnPart.toInt()

    return Seat(column = column, row = row)
}
