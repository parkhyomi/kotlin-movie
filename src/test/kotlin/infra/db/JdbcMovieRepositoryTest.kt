package infra.db

import domain.model.Movie.Movie
import infra.db.jdbc.JdbcMovieRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.util.UUID

class JdbcMovieRepositoryTest {
    @Test
    fun `saveAll 후 findAllMovies로 조회하면 영화 목록이 저장된다`() {
        val dbUrl = inMemoryUrl()
        SchemaInitializer.initializeWithUrl(dbUrl)
        val repository = JdbcMovieRepository(isLocal = false, customUrl = dbUrl)

        repository.saveAll(Movie.sampleMovies)

        val found = repository.findAllMovies()

        assertThat(found).hasSize(Movie.sampleMovies.size)
        assertThat(found.map { movie -> movie.findMovieTitle() })
            .containsExactlyElementsOf(Movie.sampleMovies.map { movie -> movie.findMovieTitle() })
    }

    @Test
    fun `findByTitle은 제목으로 영화를 조회한다`() {
        val dbUrl = inMemoryUrl()
        SchemaInitializer.initializeWithUrl(dbUrl)
        val repository = JdbcMovieRepository(isLocal = false, customUrl = dbUrl)
        repository.saveAll(Movie.sampleMovies)

        val found = repository.findByTitle("탑건: 매버릭")

        assertThat(found).isNotNull
        assertThat(found?.findMovieTitle()).isEqualTo("탑건: 매버릭")
        assertThat(found?.findRunningMinutes()).isEqualTo(130L)
    }

    @Test
    fun `파일 DB를 다시 열어도 저장된 영화를 조회할 수 있다`() {
        val dbPath = Files.createTempDirectory("cinema-h2-movie-test").resolve("movie")
        val fileUrl = "jdbc:h2:file:${dbPath.toAbsolutePath()};DB_CLOSE_ON_EXIT=FALSE"
        SchemaInitializer.initializeWithUrl(fileUrl)

        val firstRepository = JdbcMovieRepository(isLocal = false, customUrl = fileUrl)
        firstRepository.saveAll(Movie.sampleMovies)

        val secondRepository = JdbcMovieRepository(isLocal = false, customUrl = fileUrl)
        val found = secondRepository.findByTitle("아이언맨 3")

        assertThat(found).isNotNull
        assertThat(found?.findRunningMinutes()).isEqualTo(122L)
    }

    private fun inMemoryUrl(): String {
        val testDbName = "movie_test_${UUID.randomUUID().toString().replace("-", "")}"
        return "jdbc:h2:mem:$testDbName;DB_CLOSE_DELAY=-1"
    }
}
