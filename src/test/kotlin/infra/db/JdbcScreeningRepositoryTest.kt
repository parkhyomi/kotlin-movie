package infra.db

import domain.model.seat.RowLabel
import domain.model.seat.Seat
import domain.model.seat.SeatStatus
import infra.db.jdbc.JdbcMovieRepository
import infra.db.jdbc.JdbcReservationRepository
import infra.db.jdbc.JdbcScreeningRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class JdbcScreeningRepositoryTest {
    @Test
    fun `createScreening 후 findAllScreenings로 상영 목록을 조회할 수 있다`() {
        val dbUrl = inMemoryUrl()
        SchemaInitializer.initializeWithUrl(dbUrl)

        val movieRepository = JdbcMovieRepository(isLocal = false, customUrl = dbUrl)
        movieRepository.saveAll(domain.model.Movie.Movie.sampleMovies)
        val reservationRepository = JdbcReservationRepository(isLocal = false, customUrl = dbUrl)

        val repository =
            JdbcScreeningRepository(
                isLocal = false,
                customUrl = dbUrl,
                movieRepository = movieRepository,
                reservationRepository = reservationRepository,
            )
        repository.createScreening("탑건: 매버릭", LocalDate.of(2026, 4, 6), LocalTime.of(10, 0))
        repository.createScreening("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0))

        val found = repository.findAllScreenings()

        assertThat(found).hasSize(2)
        assertThat(found.map { screening -> screening.movie.findMovieTitle() })
            .containsExactly("탑건: 매버릭", "마더")
    }

    @Test
    fun `reserveSeats 후 seatStatusesOf로 좌석 예약 상태가 반영된다`() {
        val dbUrl = inMemoryUrl()
        SchemaInitializer.initializeWithUrl(dbUrl)

        val movieRepository = JdbcMovieRepository(isLocal = false, customUrl = dbUrl)
        movieRepository.saveAll(domain.model.Movie.Movie.sampleMovies)
        val reservationRepository = JdbcReservationRepository(isLocal = false, customUrl = dbUrl)

        val repository =
            JdbcScreeningRepository(
                isLocal = false,
                customUrl = dbUrl,
                movieRepository = movieRepository,
                reservationRepository = reservationRepository,
            )
        val date = LocalDate.of(2026, 4, 6)
        val startTime = LocalTime.of(16, 0)
        repository.createScreening("아이언맨 3", date, startTime)

        val firstSeat = Seat(column = 1, row = RowLabel.A)
        val secondSeat = Seat(column = 2, row = RowLabel.B)
        repository.reserveSeats("아이언맨 3", date, startTime, listOf(firstSeat, secondSeat))

        val statuses = repository.seatStatusesOf("아이언맨 3", date, startTime)

        assertThat(seatStatusOf(statuses, firstSeat)).isEqualTo(SeatStatus.RESERVED)
        assertThat(seatStatusOf(statuses, secondSeat)).isEqualTo(SeatStatus.RESERVED)
    }

    @Test
    fun `파일 DB를 다시 열어도 상영과 좌석 예약 상태를 조회할 수 있다`() {
        val dbPath = Files.createTempDirectory("cinema-h2-screening-test").resolve("screening")
        val fileUrl = "jdbc:h2:file:${dbPath.toAbsolutePath()};DB_CLOSE_ON_EXIT=FALSE"
        SchemaInitializer.initializeWithUrl(fileUrl)

        val firstMovieRepository = JdbcMovieRepository(isLocal = false, customUrl = fileUrl)
        firstMovieRepository.saveAll(domain.model.Movie.Movie.sampleMovies)
        val firstReservationRepository = JdbcReservationRepository(isLocal = false, customUrl = fileUrl)

        val firstRepository =
            JdbcScreeningRepository(
                isLocal = false,
                customUrl = fileUrl,
                movieRepository = firstMovieRepository,
                reservationRepository = firstReservationRepository,
            )
        val date = LocalDate.of(2026, 4, 8)
        val startTime = LocalTime.of(14, 0)
        firstRepository.createScreening("남은 인생 10년", date, startTime)
        val reservedSeat = Seat(column = 3, row = RowLabel.C)
        firstRepository.reserveSeats("남은 인생 10년", date, startTime, listOf(reservedSeat))

        val secondMovieRepository = JdbcMovieRepository(isLocal = false, customUrl = fileUrl)
        val secondReservationRepository = JdbcReservationRepository(isLocal = false, customUrl = fileUrl)
        val secondRepository =
            JdbcScreeningRepository(
                isLocal = false,
                customUrl = fileUrl,
                movieRepository = secondMovieRepository,
                reservationRepository = secondReservationRepository,
            )
        val statuses = secondRepository.seatStatusesOf("남은 인생 10년", date, startTime)

        assertThat(seatStatusOf(statuses, reservedSeat)).isEqualTo(SeatStatus.RESERVED)
    }

    private fun seatStatusOf(
        statuses: List<domain.model.seat.SeatAvailability>,
        targetSeat: Seat,
    ): SeatStatus =
        statuses
            .first { seatAvailability -> seatAvailability.isSeat(targetSeat) }
            .status

    private fun inMemoryUrl(): String {
        val testDbName = "screening_test_${UUID.randomUUID().toString().replace("-", "")}"
        return "jdbc:h2:mem:$testDbName;DB_CLOSE_DELAY=-1"
    }
}
