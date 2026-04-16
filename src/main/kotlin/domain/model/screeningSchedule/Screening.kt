package domain.model.screeningschedule

import domain.model.Movie.Movie
import domain.model.seat.Seat
import domain.model.seat.SeatAvailability
import domain.model.seat.SeatInventory
import java.time.LocalDate
import java.time.LocalTime

data class Screening(
    val screeningDate: LocalDate,
    val startTime: LocalTime,
    val movie: Movie,
    private val seatInventory: SeatInventory = SeatInventory(SeatInventory.defaultSeatAvailabilities()),
) {
    // 종료 시각은 영화 러닝타임으로 계산한다. (예: 12:00 + 120분 = 14:00)
    val endTime: LocalTime = startTime.plusMinutes(movie.findRunningMinutes())

    init {
        require(movie.findRunningMinutes() > 0) { "영화 상영 시간은 0보다 커야 합니다." }
        require(endTime.isAfter(startTime)) { "상영 종료 시간은 시작 시간 이후여야 합니다." }
    }

    fun isOn(date: LocalDate): Boolean = screeningDate == date

    fun isForMovie(title: String): Boolean = movie.findMovieTitle() == title

    fun startsAt(time: LocalTime): Boolean = startTime == time

    fun reserveAll(targetSeats: List<Seat>): Screening =
        targetSeats.fold(this) { screening, targetSeat ->
            screening.copy(seatInventory = screening.seatInventory.reserve(targetSeat))
        }

    fun seatStatuses(): List<SeatAvailability> = seatInventory.statuses()

    fun overlapsWith(other: Screening): Boolean {
        if (!isOn(other.screeningDate)) {
            return false
        }
        return !(endTime <= other.startTime || startTime >= other.endTime)
    }
}
