package domain.backend.repository.support

import domain.backend.repository.jdbc.JdbcMovieRepository
import domain.backend.repository.jdbc.JdbcReservationRepository
import domain.backend.repository.jdbc.JdbcScreeningRepository
import domain.model.movie.Movie
import domain.model.seat.RowLabel
import domain.model.seat.Seat
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.sql.Date
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class JdbcReservationRepositoryTest {
    @Test
    fun `reserveSeats 후 findReservedSeats로 예약 좌석을 조회할 수 있다`() {
        val dbUrl = inMemoryUrl()
        SchemaInitializer.initializeWithUrl(dbUrl)

        val movieRepository = JdbcMovieRepository(isLocal = false, customUrl = dbUrl)
        movieRepository.saveAll(Movie.sampleMovies)
        val reservationRepository = JdbcReservationRepository(isLocal = false, customUrl = dbUrl)

        val screeningRepository =
            JdbcScreeningRepository(
                isLocal = false,
                customUrl = dbUrl,
                movieRepository = movieRepository,
                reservationRepository = reservationRepository,
            )
        val date = LocalDate.of(2026, 4, 6)
        val startTime = LocalTime.of(10, 0)
        screeningRepository.createScreening("탑건: 매버릭", date, startTime)

        val screeningId = findScreeningId(dbUrl, "탑건: 매버릭", date, startTime)

        reservationRepository.reserveSeats(screeningId, listOf(Seat(column = 2, row = RowLabel.B), Seat(column = 1, row = RowLabel.A)))

        val found = reservationRepository.findReservedSeats(screeningId)

        assertThat(found).containsExactly(
            Seat(column = 1, row = RowLabel.A),
            Seat(column = 2, row = RowLabel.B),
        )
    }

    @Test
    fun `이미 예약된 좌석을 다시 reserveSeats 하면 예외가 발생한다`() {
        val dbUrl = inMemoryUrl()
        SchemaInitializer.initializeWithUrl(dbUrl)

        val movieRepository = JdbcMovieRepository(isLocal = false, customUrl = dbUrl)
        movieRepository.saveAll(Movie.sampleMovies)
        val reservationRepository = JdbcReservationRepository(isLocal = false, customUrl = dbUrl)

        val screeningRepository =
            JdbcScreeningRepository(
                isLocal = false,
                customUrl = dbUrl,
                movieRepository = movieRepository,
                reservationRepository = reservationRepository,
            )
        val date = LocalDate.of(2026, 4, 7)
        val startTime = LocalTime.of(13, 30)
        screeningRepository.createScreening("스파이더맨: 노 웨이 홈", date, startTime)

        val screeningId = findScreeningId(dbUrl, "스파이더맨: 노 웨이 홈", date, startTime)

        reservationRepository.reserveSeats(screeningId, listOf(Seat(column = 3, row = RowLabel.C)))

        assertThatThrownBy {
            reservationRepository.reserveSeats(screeningId, listOf(Seat(column = 3, row = RowLabel.C)))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("이미 예약된 좌석입니다.")
    }

    private fun findScreeningId(
        dbUrl: String,
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): Long =
        H2ConnectionFactory.connection(dbUrl).use { connection ->
            connection
                .prepareStatement(
                    """
                    SELECT s.id
                    FROM screening s
                    JOIN movie m ON s.movie_id = m.id
                    WHERE m.title = ? AND s.screening_date = ? AND s.start_time = ?
                    """.trimIndent(),
                ).use { statement ->
                    statement.setString(1, movieTitle)
                    statement.setDate(2, Date.valueOf(date))
                    statement.setTime(3, Time.valueOf(startTime))
                    statement.executeQuery().use { resultSet ->
                        if (!resultSet.next()) {
                            throw IllegalArgumentException("해당 조건의 상영이 존재하지 않습니다.")
                        }
                        resultSet.getLong("id")
                    }
                }
        }

    private fun inMemoryUrl(): String {
        val testDbName = "reservation_test_${UUID.randomUUID().toString().replace("-", "")}"
        return "jdbc:h2:mem:$testDbName;DB_CLOSE_DELAY=-1"
    }
}
