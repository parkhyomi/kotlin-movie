package domain.model.seat

data class Seat(
    val column: Int,
    val row: RowLabel,
    val seatClass: SeatClass = SeatClass.from(row),
) {
    init {
        require(column in columns) { "존재하지 않은 좌석입니다." }
        require(row in rows) { "존재하지 않은 좌석입니다." }
    }

    companion object {
        val columns = (1..12).toList()
        val rows = RowLabel.entries
    }
}

enum class SeatClass(
    val price: Int,
) {
    B(12_000),
    A(15_000),
    S(18_000),
    ;

    companion object {
        fun from(row: RowLabel): SeatClass =
            when (row) {
                RowLabel.A, RowLabel.B -> B
                RowLabel.C -> A
                RowLabel.D, RowLabel.E -> S
            }
    }
}

enum class RowLabel {
    A,
    B,
    C,
    D,
    E,
}
