package infra.db.inmemory

import domain.model.Movie.Movie
import domain.model.Movie.MovieTitle
import domain.model.Movie.RunningMinutes
import infra.db.repository.MovieRepository

object InMemoryMovie : MovieRepository {
    val TOP_GUN_MAVERICK = Movie(1L, MovieTitle("탑건: 매버릭"), RunningMinutes(130))
    val MOTHER = Movie(2L, MovieTitle("마더"), RunningMinutes(100))
    val SPIDER_MAN_NO_WAY_HOME = Movie(3L, MovieTitle("스파이더맨: 노 웨이 홈"), RunningMinutes(140))
    val THE_LAST_10_YEARS = Movie(4L, MovieTitle("남은 인생 10년"), RunningMinutes(124))
    val IRON_MAN_3 = Movie(5L, MovieTitle("아이언맨 3"), RunningMinutes(122))
    val EVEN_IF_THIS_LOVE_DISAPPEARS_TONIGHT =
        Movie(6L, MovieTitle("오늘 밤 이세상에서 사랑이 사라진다 해도"), RunningMinutes(105))
    val CHAINSAW_MAN = Movie(7L, MovieTitle("체인소맨"), RunningMinutes(101))
    val HOPPERS = Movie(8L, MovieTitle("호퍼스"), RunningMinutes(105))

    private val data: MutableList<Movie> =
        mutableListOf(
            TOP_GUN_MAVERICK,
            MOTHER,
            SPIDER_MAN_NO_WAY_HOME,
            THE_LAST_10_YEARS,
            IRON_MAN_3,
            EVEN_IF_THIS_LOVE_DISAPPEARS_TONIGHT,
            CHAINSAW_MAN,
            HOPPERS,
        )

    override fun findAllMovies(): List<Movie> = data.toList()

    override fun findByTitle(title: String): Movie? =
        data.firstOrNull { movie ->
            movie.findMovieTitle() == title
        }

    override fun saveAll(movies: List<Movie>) {
        movies.forEach { incoming ->
            val index =
                data.indexOfFirst { saved ->
                    saved.id == incoming.id
                }
            if (index >= 0) {
                data[index] = incoming
                return@forEach
            }
            data.add(incoming)
        }
    }
}
