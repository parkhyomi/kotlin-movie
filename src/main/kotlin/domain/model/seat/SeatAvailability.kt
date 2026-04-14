package domain.model.seat

data class SeatAvailability(
    val seat: Seat,
    val status: SeatStatus = SeatStatus.AVAILABLE,
) {
    fun isSeat(targetSeat: Seat): Boolean = seat == targetSeat

    fun isAvailable(): Boolean = status == SeatStatus.AVAILABLE

    fun reserve(): SeatAvailability = copy(status = SeatStatus.RESERVED)
}

enum class SeatStatus {
    AVAILABLE,
    RESERVED,
}
