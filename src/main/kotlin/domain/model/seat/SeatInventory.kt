package domain.model.seat

// 상영 1건의 전체 좌석 상태를 관리한다.
data class SeatInventory(
    private val seats: List<SeatAvailability>,
) {
    // 원하는 좌석을 받았을 때 -> 예약이 되는지 안되는지
    private fun isAvailable(targetSeat: Seat): Boolean =
        seats.any { seatAvailability ->
            seatAvailability.isSeat(targetSeat) && seatAvailability.isAvailable()
        }

    // 예약 상태를 변경
    fun reserve(targetSeat: Seat): SeatInventory {
        require(isAvailable(targetSeat)) { "이미 예약된 좌석입니다." }
        return copy(
            seats =
                seats.map { seatAvailability ->
                    if (!seatAvailability.isSeat(targetSeat)) {
                        return@map seatAvailability
                    }
                    seatAvailability.reserve()
                },
        )
    }

    // 현재 좌석 상태 목록을 반환한다.
    fun statuses(): List<SeatAvailability> = seats

    companion object {
        // 스크린당 좌석의 초기세팅
        fun defaultSeatAvailabilities(): List<SeatAvailability> =
            Seat.columns.flatMap { column ->
                Seat.rows.map { row ->
                    SeatAvailability(
                        seat =
                            Seat(
                                column = column,
                                row = row,
                            ),
                    )
                }
            }
    }
}
