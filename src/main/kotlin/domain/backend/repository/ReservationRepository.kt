package domain.backend.repository

import domain.model.seat.Seat

interface ReservationRepository {
    fun findReservedSeats(screeningId: Long): List<Seat>

    fun reserveSeats(
        screeningId: Long,
        seats: List<Seat>,
    )
}
