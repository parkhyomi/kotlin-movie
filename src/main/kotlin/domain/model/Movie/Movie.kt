package domain.model.movie

data class Movie(
    val id: Long? = null,
    val movieTitle: MovieTitle,
    val runningMinutes: RunningMinutes,
) {
    constructor(
        title: String,
        runningMinutes: Int,
    ) : this(
        id = null,
        movieTitle = MovieTitle(title),
        runningMinutes = RunningMinutes(runningMinutes.toLong()),
    )

    constructor(
        title: String,
        runningMinutes: Long,
    ) : this(
        id = null,
        movieTitle = MovieTitle(title),
        runningMinutes = RunningMinutes(runningMinutes),
    )

    init {
        require(runningMinutes.value > 0) { "영화 러닝타임은 0보다 커야 합니다." }
    }

    fun findMovieTitle(): String = movieTitle.value

    fun findRunningMinutes(): Long = runningMinutes.value

    companion object {
        val sampleMovies: List<Movie> =
            listOf(
                Movie(1L, MovieTitle("탑건: 매버릭"), RunningMinutes(130)),
                Movie(2L, MovieTitle("마더"), RunningMinutes(100)),
                Movie(3L, MovieTitle("스파이더맨: 노 웨이 홈"), RunningMinutes(140)),
                Movie(4L, MovieTitle("남은 인생 10년"), RunningMinutes(124)),
                Movie(5L, MovieTitle("아이언맨 3"), RunningMinutes(122)),
                Movie(6L, MovieTitle("오늘 밤 이세상에서 사랑이 사라진다 해도"), RunningMinutes(105)),
                Movie(7L, MovieTitle("체인소맨"), RunningMinutes(101)),
                Movie(8L, MovieTitle("호퍼스"), RunningMinutes(105)),
            )
    }
}
