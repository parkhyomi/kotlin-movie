package domain.backend.service

import domain.backend.dto.MovieResponse
import domain.backend.dto.ScreeningResponse
import domain.backend.repository.ScreeningCatalogQueryRepository
import org.springframework.stereotype.Service

@Service
class MovieApiService(
    private val screeningCatalogQueryRepository: ScreeningCatalogQueryRepository,
) {
    fun findMovies(): List<MovieResponse> =
        screeningCatalogQueryRepository
            .findAllMoviesWithScreenings()
            .map { movie ->
                MovieResponse(
                    id = movie.id,
                    title = movie.title,
                    runningTimeMinutes = movie.runningTimeMinutes,
                    screenings =
                        movie.screenings.map { screening ->
                            ScreeningResponse(
                                id = screening.id,
                                startAt = screening.startAt,
                                endAt = screening.endAt,
                            )
                        },
                )
            }
}
