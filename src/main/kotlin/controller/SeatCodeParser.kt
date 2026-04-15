package controller

import domain.model.seat.Seat
import domain.model.seat.parseSeats

interface SeatCodeParser {
    fun parse(seatCodes: List<String>): List<Seat>
}

class DefaultSeatCodeParser : SeatCodeParser {
    override fun parse(seatCodes: List<String>): List<Seat> = parseSeats(seatCodes)
}
