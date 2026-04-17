package domain.backend.repository

import domain.model.screeningschedule.Screening
import java.time.LocalDateTime

interface ScreeningCatalogQueryRepository {
    fun findAllMoviesWithScreenings(): List<MovieCatalog>

    fun findScreeningById(screeningId: Long): Screening?
}

data class MovieCatalog(
    val id: Long,
    val title: String,
    val runningTimeMinutes: Long,
    val screenings: List<ScreeningCatalog>,
)

data class ScreeningCatalog(
    val id: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
)
