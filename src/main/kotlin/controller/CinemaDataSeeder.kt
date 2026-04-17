package controller

import domain.model.movie.Movie
import infra.db.repository.MovieRepository
import infra.db.repository.ScreeningRepository
import java.time.LocalDate
import java.time.LocalTime

class CinemaDataSeeder(
    private val movieRepository: MovieRepository,
    private val screeningRepository: ScreeningRepository,
) {
    fun seedIfNeeded() {
        if (movieRepository.findAllMovies().isEmpty()) {
            movieRepository.saveAll(Movie.sampleMovies)
        }

        if (screeningRepository.findAllScreenings().isNotEmpty()) {
            return
        }

        defaultSeeds.forEach { seed ->
            screeningRepository.createScreening(
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
