package controller

import infra.db.SchemaInitializer
import infra.db.inmemory.InMemoryMovie
import infra.db.inmemory.InMemoryReservationRepository
import infra.db.inmemory.InMemoryScreeningRepository
import infra.db.jdbc.JdbcMovieRepository
import infra.db.jdbc.JdbcReservationRepository
import infra.db.jdbc.JdbcScreeningRepository

object CinemaControllerFactory {
    fun withLocalDatabase(): CinemaController = createJdbcController(isLocal = true, customUrl = null)

    fun withJdbc(customUrl: String): CinemaController = createJdbcController(isLocal = false, customUrl = customUrl)

    fun withInMemory(): CinemaController {
        val movieRepository = InMemoryMovie
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
