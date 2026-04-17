package infra.db.jdbc

import domain.model.seat.RowLabel
import domain.model.seat.Seat
import infra.db.H2ConnectionFactory
import infra.db.repository.ReservationRepository
import java.sql.Connection
import kotlin.use

class JdbcReservationRepository(
    private val isLocal: Boolean = true,
    private val customUrl: String? = null,
) : ReservationRepository {
    override fun findReservedSeats(screeningId: Long): List<Seat> =
        connection().use { connection ->
            findReservedSeats(connection, screeningId)
        }

    override fun reserveSeats(
        screeningId: Long,
        seats: List<Seat>,
    ) {
        if (seats.isEmpty()) {
            return
        }

        connection().use { connection ->
            connection.autoCommit = false
            try {
                val alreadyReserved = findReservedSeats(connection, screeningId).toSet()
                require(seats.none { seat -> alreadyReserved.contains(seat) }) { "이미 예약된 좌석입니다." }

                connection
                    .prepareStatement(
                        """
                        INSERT INTO reservation (screening_id, seat_row, seat_column)
                        VALUES (?, ?, ?)
                        """.trimIndent(),
                    ).use { statement ->
                        seats.forEach { seat ->
                            statement.setLong(1, screeningId)
                            statement.setString(2, seat.row.name)
                            statement.setInt(3, seat.column)
                            statement.addBatch()
                        }
                        statement.executeBatch()
                    }

                connection.commit()
            } catch (exception: Exception) {
                connection.rollback()
                throw exception
            } finally {
                connection.autoCommit = true
            }
        }
    }

    private fun findReservedSeats(
        connection: Connection,
        screeningId: Long,
    ): List<Seat> =
        connection
            .prepareStatement(
                """
                SELECT seat_row, seat_column
                FROM reservation
                WHERE screening_id = ?
                ORDER BY seat_row, seat_column
                """.trimIndent(),
            ).use { statement ->
                statement.setLong(1, screeningId)
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(
                                Seat(
                                    column = resultSet.getInt("seat_column"),
                                    row = RowLabel.valueOf(resultSet.getString("seat_row")),
                                ),
                            )
                        }
                    }
                }
            }

    private fun connection(): Connection =
        customUrl?.let { url ->
            H2ConnectionFactory.connection(url)
        } ?: H2ConnectionFactory.connection(isLocal)
}
