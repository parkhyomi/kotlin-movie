package domain.backend.repository.jdbc

import domain.backend.repository.MovieRepository
import domain.backend.repository.ReservationRepository
import domain.backend.repository.ScreeningRepository
import domain.backend.repository.support.H2ConnectionFactory
import domain.model.movie.Movie
import domain.model.movie.MovieTitle
import domain.model.movie.RunningMinutes
import domain.model.screeningschedule.Screening
import domain.model.screeningschedule.policy.DefaultScreeningCreationPolicy
import domain.model.screeningschedule.policy.ScreenPeriod
import domain.model.screeningschedule.policy.ScreeningCreationPolicy
import domain.model.seat.Seat
import domain.model.seat.SeatAvailability
import java.sql.Connection
import java.sql.Date
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime
import kotlin.use

class JdbcScreeningRepository(
    private val isLocal: Boolean = true,
    private val customUrl: String? = null,
    private val movieRepository: MovieRepository,
    private val reservationRepository: ReservationRepository,
    private val screenPeriod: ScreenPeriod = ScreenPeriod(),
    private val screeningCreationPolicy: ScreeningCreationPolicy = DefaultScreeningCreationPolicy(),
) : ScreeningRepository {
    override fun findAllScreenings(): List<Screening> =
        connection().use { connection ->
            findAllRows(connection)
                .sortedWith(compareBy<ScreeningRow>({ it.screeningDate }, { it.startTime }, { it.movieTitle }))
                .map { row -> toScreening(row) }
        }

    override fun saveAll(screenings: List<Screening>) {
        screenings.forEach { screening ->
            val movieTitle = screening.movie.findMovieTitle()
            val screeningDate = screening.screeningDate
            val startTime = screening.startTime

            val screeningId =
                findScreeningRow(movieTitle, screeningDate, startTime)?.id
                    ?: createScreening(movieTitle, screeningDate, startTime).let {
                        findScreeningRow(movieTitle, screeningDate, startTime)?.id
                            ?: throw IllegalArgumentException("상영 저장에 실패했습니다.")
                    }

            val reservedSeats =
                screening
                    .seatStatuses()
                    .filter { seatAvailability -> !seatAvailability.isAvailable() }
                    .map { seatAvailability -> seatAvailability.seat }

            if (reservedSeats.isNotEmpty()) {
                reservationRepository.reserveSeats(screeningId, reservedSeats)
            }
        }
    }

    override fun screeningsOfMovieTitle(movieTitle: String): List<Screening> =
        connection().use { connection ->
            findRowsByMovieTitle(connection, movieTitle)
                .sortedWith(compareBy<ScreeningRow>({ it.screeningDate }, { it.startTime }))
                .map { row -> toScreening(row) }
        }

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

        connection().use { connection ->
            connection
                .prepareStatement(
                    """
                    INSERT INTO screening (movie_id, screening_date, start_time)
                    VALUES (?, ?, ?)
                    """.trimIndent(),
                ).use { statement ->
                    statement.setLong(1, movie.id ?: throw IllegalArgumentException("영화 ID가 없습니다."))
                    statement.setDate(2, Date.valueOf(screeningDate))
                    statement.setTime(3, Time.valueOf(startTime))
                    statement.executeUpdate()
                }
        }

        val savedRow =
            findScreeningRow(movieTitle, screeningDate, startTime)
                ?: throw IllegalArgumentException("상영 저장에 실패했습니다.")

        return toScreening(savedRow)
    }

    private fun findAllRows(connection: Connection): List<ScreeningRow> =
        connection
            .prepareStatement(
                """
                SELECT s.id, s.screening_date, s.start_time, m.id AS movie_id, m.title, m.running_minutes
                FROM screening s
                JOIN movie m ON s.movie_id = m.id
                """.trimIndent(),
            ).use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(resultSet.toScreeningRow())
                        }
                    }
                }
            }

    private fun findRowsByMovieTitle(
        connection: Connection,
        movieTitle: String,
    ): List<ScreeningRow> =
        connection
            .prepareStatement(
                """
                SELECT s.id, s.screening_date, s.start_time, m.id AS movie_id, m.title, m.running_minutes
                FROM screening s
                JOIN movie m ON s.movie_id = m.id
                WHERE m.title = ?
                """.trimIndent(),
            ).use { statement ->
                statement.setString(1, movieTitle)
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(resultSet.toScreeningRow())
                        }
                    }
                }
            }

    private fun findScreeningRow(
        movieTitle: String,
        date: LocalDate,
        startTime: LocalTime,
    ): ScreeningRow? =
        connection().use { connection ->
            connection
                .prepareStatement(
                    """
                    SELECT s.id, s.screening_date, s.start_time, m.id AS movie_id, m.title, m.running_minutes
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
                            return null
                        }
                        resultSet.toScreeningRow()
                    }
                }
        }

    private fun toScreening(screeningRow: ScreeningRow): Screening {
        val base =
            Screening(
                screeningDate = screeningRow.screeningDate,
                startTime = screeningRow.startTime,
                movie =
                    Movie(
                        id = screeningRow.movieId,
                        movieTitle = MovieTitle(screeningRow.movieTitle),
                        runningMinutes = RunningMinutes(screeningRow.runningMinutes),
                    ),
            )

        val reservedSeats = reservationRepository.findReservedSeats(screeningRow.id)
        if (reservedSeats.isEmpty()) {
            return base
        }
        return base.reserveAll(reservedSeats)
    }

    private fun java.sql.ResultSet.toScreeningRow(): ScreeningRow =
        ScreeningRow(
            id = getLong("id"),
            movieId = getLong("movie_id"),
            movieTitle = getString("title"),
            runningMinutes = getLong("running_minutes"),
            screeningDate = getDate("screening_date").toLocalDate(),
            startTime = getTime("start_time").toLocalTime(),
        )

    private fun connection(): Connection =
        customUrl?.let { url ->
            H2ConnectionFactory.connection(url)
        } ?: H2ConnectionFactory.connection(isLocal)

    private data class ScreeningRow(
        val id: Long,
        val movieId: Long,
        val movieTitle: String,
        val runningMinutes: Long,
        val screeningDate: LocalDate,
        val startTime: LocalTime,
    )
}
