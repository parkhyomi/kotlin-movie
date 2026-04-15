package view

import domain.model.seat.RowLabel
import domain.model.seat.SeatAvailability

object SeatLayoutView {
    fun render(seatStatuses: List<SeatAvailability>) {
        require(seatStatuses.isNotEmpty()) { "좌석 정보가 없습니다." }

        val columns = seatStatuses.map { it.seat.column }.distinct().sorted()
        val rows = seatStatuses.map { it.seat.row }.distinct().sortedBy { it.ordinal }
        val seatByPosition = seatStatuses.associateBy { it.seat.row to it.seat.column }

        headers(columns)
        bodys(rows, columns, seatByPosition)
    }

    private fun headers(columns: List<Int>) {
        print("    ")
        columns.forEach { column ->
            print("$column    ")
        }
        println()
    }

    private fun bodys(
        rows: List<RowLabel>,
        columns: List<Int>,
        seatByPosition: Map<Pair<RowLabel, Int>, SeatAvailability>,
    ) {
        rows.forEach { row ->
            print("${row.name} ")
            columns.forEach { column ->
                val seatAvailability =
                    seatByPosition[row to column]
                        ?: throw IllegalArgumentException("좌석 정보가 누락되었습니다.")
                print("[ ${seatAvailability.seat.seatClass.name}] ")
            }
            println()
        }
    }
}
