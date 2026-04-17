package domain.backend.factory

import domain.backend.facade.CinemaController
import domain.backend.repository.inmemory.InMemoryMovieRepository
import domain.backend.repository.inmemory.InMemoryReservationRepository
import domain.backend.repository.inmemory.InMemoryScreeningRepository
import domain.backend.repository.jdbc.JdbcMovieRepository
import domain.backend.repository.jdbc.JdbcReservationRepository
import domain.backend.repository.jdbc.JdbcScreeningRepository
import domain.backend.repository.support.SchemaInitializer
import global.initdata.CinemaDataSeeder

object CinemaControllerFactory {
    fun withLocalDatabase(): CinemaController = createJdbcController(isLocal = true, customUrl = null)

    fun withJdbc(customUrl: String): CinemaController = createJdbcController(isLocal = false, customUrl = customUrl)

    fun withInMemory(): CinemaController {
        val movieRepository = InMemoryMovieRepository
        val reservationRepository = InMemoryReservationRepository()
        val screeningRepository =
            InMemoryScreeningRepository(
                movieRepository = movieRepository,
                reservationRepository = reservationRepository,
            )

        CinemaDataSeeder(
            movieRepository = movieRepository,
            screeningRepository = screeningRepository,
        ).seedIfNeeded()

        return CinemaController(screeningRepository = screeningRepository)
    }

    private fun createJdbcController(
        isLocal: Boolean,
        customUrl: String?,
    ): CinemaController {
        customUrl?.let { url ->
            SchemaInitializer.initializeWithUrl(url)
        } ?: SchemaInitializer.initialize(isLocal = isLocal)

        val movieRepository = JdbcMovieRepository(isLocal = isLocal, customUrl = customUrl)
        val reservationRepository = JdbcReservationRepository(isLocal = isLocal, customUrl = customUrl)
        val screeningRepository =
            JdbcScreeningRepository(
                isLocal = isLocal,
                customUrl = customUrl,
                movieRepository = movieRepository,
                reservationRepository = reservationRepository,
            )

        CinemaDataSeeder(
            movieRepository = movieRepository,
            screeningRepository = screeningRepository,
        ).seedIfNeeded()

        return CinemaController(screeningRepository = screeningRepository)
    }
}
