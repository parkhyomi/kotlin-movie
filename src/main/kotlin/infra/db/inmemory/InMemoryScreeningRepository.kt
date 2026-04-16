package infra.db.inmemory

import domain.model.screeningschedule.Screening
import domain.model.screeningschedule.policy.DefaultScreeningCreationPolicy
import domain.model.screeningschedule.policy.ScreenPeriod
import domain.model.screeningschedule.policy.ScreeningCreationPolicy
import domain.model.seat.Seat
import domain.model.seat.SeatAvailability
import infra.db.repository.MovieRepository
import infra.db.repository.ReservationRepository
import infra.db.repository.ScreeningRepository
import java.time.LocalDate
import java.time.LocalTime

class InMemoryScreeningRepository(
    private val movieRepository: MovieRepository = InMemoryMovie,
    private val reservationRepository: ReservationRepository = InMemoryReservationRepository(),
    private val screenPeriod: ScreenPeriod = ScreenPeriod(),
    private val screeningCreationPolicy: ScreeningCreationPolicy = DefaultScreeningCreationPolicy(),
) : ScreeningRepository {

    private val screenings: MutableList<ScreeningRow> = mutableListOf()
    private var nextId: Long = 1L

    init {
        seedDefaultScreenings()
    }

    override fun findAllScreenings(): List<Screening> =
        screenings
            .sortedWith(compareBy<ScreeningRow>({ it.screeningDate }, { it.startTime }, { it.movieTitle }))
            .map { row -> toScreening(row) }

    override fun saveAll(screenings: List<Screening>) {
        screenings.forEach { screening ->
            val screeningId =
                findScreeningRow(
                    movieTitle = screening.movie.findMovieTitle(),
                    date = screening.screeningDate,
                    startTime = screening.startTime,
                )?.id ?: createScreening(
                    movieTitle = screening.movie.findMovieTitle(),
                    screeningDate = screening.screeningDate,
                    startTime = screening.startTime,
                ).let {
                    findScreeningRow(
                        movieTitle = screening.movie.findMovieTitle(),
                        date = screening.screeningDate,
                        startTime = screening.startTime,
                    )?.id ?: throw IllegalArgumentException("상영 저장에 실패했습니다.")
                }

            val reservedSeats =
                screening.seatStatuses()
                    .filter { seatAvailability -> !seatAvailability.isAvailable() }
                    .map { seatAvailability -> seatAvailability.seat }

            if (reservedSeats.isNotEmpty()) {
                reservationRepository.reserveSeats(screeningId, reservedSeats)
            }
        }
    }

    override fun screeningsOfMovieTitle(movieTitle: String): List<Screening> =
        screenings
            .filter { screening -> screening.movieTitle == movieTitle }
            .sortedWith(compareBy<ScreeningRow>({ it.screeningDate }, { it.startTime }))
            .map { row -> toScreening(row) }

    override fun screeningsOfMovieDate(
        screenings: List<Screening>,
        date: LocalDate,
    ): List<Screening> =
        screenings.filter { screening ->
            screening.isOn(date)
        }

    override fun seatStatusesOf(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): List<SeatAvailability> =
        toScreening(
            findScreeningRow(movieTitle, date, startTime)
                ?: throw IllegalArgumentException("해당 조건의 상영이 존재하지 않습니다."),
        ).seatStatuses()

    override fun reserveSeats(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
        seats: List<Seat>,
    ): Screening {
        val screeningRow =
            findScreeningRow(movieTitle, date, startTime)
                ?: throw IllegalArgumentException("해당 조건의 상영이 존재하지 않습니다.")
        reservationRepository.reserveSeats(screeningRow.id, seats)
        return toScreening(screeningRow)
    }

    override fun createScreening(
        movieTitle: String,
        screeningDate: LocalDate,
        startTime: LocalTime,
    ): Screening {
        val movie =
            movieRepository.findByTitle(movieTitle)
                ?: throw IllegalArgumentException("존재하지 않는 영화입니다.")

        val candidate =
            Screening(
                screeningDate = screeningDate,
                startTime = startTime,
                movie = movie,
            )

        screeningCreationPolicy.validate(
            candidate = candidate,
            existing = findAllScreenings(),
            screenPeriod = screenPeriod,
        )

        val newRow =
            ScreeningRow(
                id = nextId++,
                movieTitle = movieTitle,
                screeningDate = screeningDate,
                startTime = startTime,
            )
        screenings.add(newRow)
        return toScreening(newRow)
    }

    private fun findScreeningRow(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): ScreeningRow? =
        screenings.firstOrNull { screening ->
            screening.movieTitle == movieTitle &&
                screening.screeningDate == date &&
                screening.startTime == startTime
        }

    private fun toScreening(screeningRow: ScreeningRow): Screening {
        val movie =
            movieRepository.findByTitle(screeningRow.movieTitle)
                ?: throw IllegalArgumentException("존재하지 않는 영화입니다.")
        val base =
            Screening(
                screeningDate = screeningRow.screeningDate,
                startTime = screeningRow.startTime,
                movie = movie,
            )
        val reservedSeats = reservationRepository.findReservedSeats(screeningRow.id)
        if (reservedSeats.isEmpty()) {
            return base
        }
        return base.reserveAll(reservedSeats)
    }

    private data class ScreeningRow(
        val id: Long,
        val movieTitle: String,
        val screeningDate: LocalDate,
        val startTime: LocalTime,
    )

    private fun seedDefaultScreenings() {
        if (screenings.isNotEmpty()) {
            return
        }
        defaultSeeds.forEach { seed ->
            createScreening(
                movieTitle = seed.movieTitle,
                screeningDate = seed.screeningDate,
                startTime = seed.startTime,
            )
        }
    }

    private data class ScreeningSeed(
        val movieTitle: String,
        val screeningDate: LocalDate,
        val startTime: LocalTime,
    )

    companion object {
        private val defaultSeeds: List<ScreeningSeed> =
            listOf(
                ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0)),
                ScreeningSeed("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0)),
                ScreeningSeed("아이언맨 3", LocalDate.of(2026, 4, 6), LocalTime.of(16, 0)),
                ScreeningSeed("아이언맨 3", LocalDate.of(2026, 4, 6), LocalTime.of(19, 0)),
                ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 7), LocalTime.of(10, 0)),
                ScreeningSeed("스파이더맨: 노 웨이 홈", LocalDate.of(2026, 4, 7), LocalTime.of(13, 30)),
                ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 8), LocalTime.of(10, 0)),
                ScreeningSeed("남은 인생 10년", LocalDate.of(2026, 4, 8), LocalTime.of(14, 0)),
                ScreeningSeed("오늘 밤 이세상에서 사랑이 사라진다 해도", LocalDate.of(2026, 4, 9), LocalTime.of(12, 20)),
                ScreeningSeed("아이언맨 3", LocalDate.of(2026, 4, 10), LocalTime.of(9, 50)),
                ScreeningSeed("체인소맨", LocalDate.of(2026, 4, 10), LocalTime.of(16, 0)),
                ScreeningSeed("호퍼스", LocalDate.of(2026, 4, 11), LocalTime.of(20, 10)),
                ScreeningSeed("스파이더맨: 노 웨이 홈", LocalDate.of(2026, 4, 12), LocalTime.of(15, 40)),
                ScreeningSeed("마더", LocalDate.of(2026, 4, 13), LocalTime.of(11, 0)),
            )
    }
}
