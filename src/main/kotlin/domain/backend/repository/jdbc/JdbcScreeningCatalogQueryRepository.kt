package domain.backend.repository.jdbc

import domain.backend.repository.MovieCatalog
import domain.backend.repository.ScreeningCatalog
import domain.backend.repository.ScreeningCatalogQueryRepository
import domain.backend.repository.support.H2ConnectionFactory
import domain.model.movie.Movie
import domain.model.movie.MovieTitle
import domain.model.movie.RunningMinutes
import domain.model.screeningschedule.Screening
import java.sql.Connection
import java.time.LocalDateTime
import kotlin.use

class JdbcScreeningCatalogQueryRepository(
    private val isLocal: Boolean = true,
    private val customUrl: String? = null,
) : ScreeningCatalogQueryRepository {
    override fun findAllMoviesWithScreenings(): List<MovieCatalog> =
        connection().use { connection ->
            connection
                .prepareStatement(
                    """
                    SELECT
                        m.id AS movie_id,
                        m.title AS movie_title,
                        m.running_minutes,
                        s.id AS screening_id,
                        s.screening_date,
                        s.start_time
                    FROM movie m
                    LEFT JOIN screening s ON s.movie_id = m.id
                    ORDER BY m.id, s.screening_date, s.start_time
                    """.trimIndent(),
                ).use { statement ->
                    statement.executeQuery().use { resultSet ->
                        val grouped = linkedMapOf<Long, MutableMovieCatalog>()

                        while (resultSet.next()) {
                            val movieId = resultSet.getLong("movie_id")
                            val title = resultSet.getString("movie_title")
                            val runningMinutes = resultSet.getLong("running_minutes")

                            val movieEntry =
                                grouped.getOrPut(movieId) {
                                    MutableMovieCatalog(
                                        id = movieId,
                                        title = title,
                                        runningTimeMinutes = runningMinutes,
                                    )
                                }

                            val screeningId = resultSet.getLong("screening_id")
                            if (resultSet.wasNull()) {
                                continue
                            }

                            val screeningDate = resultSet.getDate("screening_date").toLocalDate()
                            val startTime = resultSet.getTime("start_time").toLocalTime()
                            val startAt = LocalDateTime.of(screeningDate, startTime)
                            val endAt = startAt.plusMinutes(runningMinutes)

                            movieEntry.screenings.add(
                                ScreeningCatalog(
                                    id = screeningId,
                                    startAt = startAt,
                                    endAt = endAt,
                                ),
                            )
                        }

                        grouped.values.map { movie ->
                            MovieCatalog(
                                id = movie.id,
                                title = movie.title,
                                runningTimeMinutes = movie.runningTimeMinutes,
                                screenings = movie.screenings.toList(),
                            )
                        }
                    }
                }
        }

    override fun findScreeningById(screeningId: Long): Screening? =
        connection().use { connection ->
            connection
                .prepareStatement(
                    """
                    SELECT
                        m.id AS movie_id,
                        m.title AS movie_title,
                        m.running_minutes,
                        s.screening_date,
                        s.start_time
                    FROM screening s
                    JOIN movie m ON s.movie_id = m.id
                    WHERE s.id = ?
                    """.trimIndent(),
                ).use { statement ->
                    statement.setLong(1, screeningId)
                    statement.executeQuery().use { resultSet ->
                        if (!resultSet.next()) {
                            return null
                        }

                        val movie =
                            Movie(
                                id = resultSet.getLong("movie_id"),
                                movieTitle = MovieTitle(resultSet.getString("movie_title")),
                                runningMinutes = RunningMinutes(resultSet.getLong("running_minutes")),
                            )

                        Screening(
                            screeningDate = resultSet.getDate("screening_date").toLocalDate(),
                            startTime = resultSet.getTime("start_time").toLocalTime(),
                            movie = movie,
                        )
                    }
                }
        }

    private fun connection(): Connection =
        customUrl?.let { url ->
            H2ConnectionFactory.connection(url)
        } ?: H2ConnectionFactory.connection(isLocal)

    private data class MutableMovieCatalog(
        val id: Long,
        val title: String,
        val runningTimeMinutes: Long,
        val screenings: MutableList<ScreeningCatalog> = mutableListOf(),
    )
}
