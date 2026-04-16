package infra.db.jdbc

import domain.model.Movie.Movie
import domain.model.Movie.MovieTitle
import domain.model.Movie.RunningMinutes
import infra.db.H2ConnectionFactory
import infra.db.repository.MovieRepository
import java.sql.Connection
import kotlin.use

class JdbcMovieRepository(
    private val isLocal: Boolean = true,
    private val customUrl: String? = null,
) : MovieRepository {
    override fun findAllMovies(): List<Movie> =
        connection().use { connection ->
            connection.prepareStatement("SELECT id, title, running_minutes FROM movie ORDER BY id").use { statement ->
                statement.executeQuery().use { resultSet ->
                    buildList {
                        while (resultSet.next()) {
                            add(
                                Movie(
                                    id = resultSet.getLong("id"),
                                    movieTitle = MovieTitle(resultSet.getString("title")),
                                    runningMinutes = RunningMinutes(resultSet.getLong("running_minutes")),
                                ),
                            )
                        }
                    }
                }
            }
        }

    override fun findByTitle(title: String): Movie? =
        connection().use { connection ->
            connection
                .prepareStatement("SELECT id, title, running_minutes FROM movie WHERE title = ?")
                .use { statement ->
                    statement.setString(1, title)
                    statement.executeQuery().use { resultSet ->
                        if (!resultSet.next()) {
                            return null
                        }
                        Movie(
                            id = resultSet.getLong("id"),
                            movieTitle = MovieTitle(resultSet.getString("title")),
                            runningMinutes = RunningMinutes(resultSet.getLong("running_minutes")),
                        )
                    }
                }
        }

    override fun saveAll(movies: List<Movie>) {
        connection().use { connection ->
            connection.autoCommit = false
            try {
                connection
                    .prepareStatement(
                        """
                        MERGE INTO movie (id, title, running_minutes) KEY (id)
                        VALUES (?, ?, ?)
                        """.trimIndent(),
                    ).use { statement ->
                        movies.forEach { movie ->
                            statement.setLong(1, movie.id ?: 0L)
                            statement.setString(2, movie.findMovieTitle())
                            statement.setLong(3, movie.findRunningMinutes())
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

    private fun connection(): Connection =
        customUrl?.let { url ->
            H2ConnectionFactory.connection(url)
        } ?: H2ConnectionFactory.connection(isLocal)
}
