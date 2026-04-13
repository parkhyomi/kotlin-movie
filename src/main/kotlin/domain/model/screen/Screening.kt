package domain.model.screen

import domain.model.Movie
import domain.model.seat.Seat
import domain.model.seat.SeatAvailability
import domain.model.seat.SeatInventory
import java.time.LocalDate
import java.time.LocalTime

// 실제 상영 1건(날짜 + 시작/종료 시각 + 좌석 상태)을 나타낸다.
data class Screening(
    // 상영 날짜 (예: 2026-04-06)
    val screeningDate: LocalDate,
    // 해당 날짜의 시작 상영 시각 (예: 12:00)
    val startTime: LocalTime,
    val movie: Movie,
    private val seatInventory: SeatInventory = SeatInventory(SeatInventory.Companion.defaultSeatAvailabilities()),
) {
    // 종료 시각은 영화 러닝타임으로 계산한다. (예: 12:00 + 120분 = 14:00)
    val endTime: LocalTime = startTime.plusMinutes(movie.runningMinutes.toLong())

    init {
        require(movie.runningMinutes > 0) { "영화 상영 시간은 0보다 커야 합니다." }
        require(endTime.isAfter(startTime)) { "상영 종료 시간은 시작 시간 이후여야 합니다." }
    }

    // 사용자 선택 검증 -> 특정 날짜 상영인지 확인한다.
    fun isOn(date: LocalDate): Boolean = screeningDate == date

    // 사용자 선택 검증 -> 특정 영화 제목 상영인지 확인한다.
    fun isForMovie(title: String): Boolean = movie.title == title

    // 사용자 선택 검증 -> 특정 시작 시각 상영인지 확인한다.
    fun startsAt(time: LocalTime): Boolean = startTime == time

    // 여러 좌석을 순차적으로 예약한 새 상영 객체를 반환한다.
    fun reserveAll(targetSeats: List<Seat>): Screening =
        targetSeats.fold(this) { screening, targetSeat ->
            screening.reserve(targetSeat)
        }

    // 좌석 전체 상태를 반환한다.
    fun seatStatuses(): List<SeatAvailability> = seatInventory.statuses()

    // 같은 날짜의 상영끼리 시간 겹침 여부를 확인한다.
    fun overlapsWith(other: Screening): Boolean {
        if (!isOn(other.screeningDate)) {
            return false
        }
        return !(endTime <= other.startTime || startTime >= other.endTime)
    }

    // 좌석 1개를 예약상태 변경하고, 새 상영 객체를 반환한다.
    private fun reserve(targetSeat: Seat): Screening = copy(seatInventory = seatInventory.reserve(targetSeat))
}
