package domain.backend.config

import domain.backend.repository.MovieRepository
import domain.backend.repository.ScreeningRepository
import domain.backend.repository.support.SchemaInitializer
import global.initdata.CinemaDataSeeder
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class CinemaDatabaseBootstrap(
    private val dbProperties: CinemaDbProperties,
    private val movieRepository: MovieRepository,
    private val screeningRepository: ScreeningRepository,
) {
    @PostConstruct
    fun initialize() {
        // 앱 시작 시 스키마를 먼저 보장한다. (movie, screening, reservation)
        val customUrl = dbProperties.url
        if (customUrl != null) {
            SchemaInitializer.initializeWithUrl(customUrl)
        } else {
            SchemaInitializer.initialize(isLocal = dbProperties.local)
        }

        // 데이터가 비어 있으면 기존 시드 데이터를 채운다.
        CinemaDataSeeder(
            movieRepository = movieRepository,
            screeningRepository = screeningRepository,
        ).seedIfNeeded()
    }
}
