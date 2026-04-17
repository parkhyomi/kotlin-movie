package domain.backend.repository.inmemory

import domain.backend.repository.ReservationRepository
import domain.model.seat.Seat

class InMemoryReservationRepository : ReservationRepository {
    private val reservedByScreening: MutableMap<Long, MutableSet<Seat>> = mutableMapOf()

    override fun findReservedSeats(screeningId: Long): List<Seat> =
        reservedByScreening[screeningId]
            ?.toList()
            ?.sortedWith(compareBy<Seat>({ it.row.ordinal }, { it.column }))
            ?: emptyList()

    override fun reserveSeats(
        screeningId: Long,
        seats: List<Seat>,
    ) {
        if (seats.isEmpty()) {
            return
        }
        val bucket = reservedByScreening.getOrPut(screeningId) { mutableSetOf() }
        require(seats.none { seat -> bucket.contains(seat) }) { "이미 예약된 좌석입니다." }
        bucket.addAll(seats)
    }
}
