package domain.backend.config

import domain.backend.parser.DefaultSeatCodeParser
import domain.backend.repository.MovieRepository
import domain.backend.repository.ReservationRepository
import domain.backend.repository.ScreeningCatalogQueryRepository
import domain.backend.repository.ScreeningRepository
import domain.backend.repository.jdbc.JdbcMovieRepository
import domain.backend.repository.jdbc.JdbcReservationRepository
import domain.backend.repository.jdbc.JdbcScreeningCatalogQueryRepository
import domain.backend.repository.jdbc.JdbcScreeningRepository
import domain.model.payment.PaymentCalculator
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CinemaDbProperties::class)
class CinemaRepositoryConfig(
    private val dbProperties: CinemaDbProperties,
) {
    // 기존 3단계 JDBC 리포지토리를 Bean으로 등록한다.
    // 도메인 로직은 유지하고, HTTP 계층은 이 리포지토리들을 호출만 한다.
    @Bean
    fun movieRepository(): MovieRepository =
        JdbcMovieRepository(
            isLocal = isLocal(),
            customUrl = customUrl(),
        )

    @Bean
    fun reservationRepository(): ReservationRepository =
        JdbcReservationRepository(
            isLocal = isLocal(),
            customUrl = customUrl(),
        )

    @Bean
    fun screeningRepository(
        movieRepository: MovieRepository,
        reservationRepository: ReservationRepository,
    ): ScreeningRepository =
        JdbcScreeningRepository(
            isLocal = isLocal(),
            customUrl = customUrl(),
            movieRepository = movieRepository,
            reservationRepository = reservationRepository,
        )

    @Bean
    fun screeningCatalogQueryRepository(): ScreeningCatalogQueryRepository =
        JdbcScreeningCatalogQueryRepository(
            isLocal = isLocal(),
            customUrl = customUrl(),
        )

    @Bean
    fun paymentCalculator(): PaymentCalculator = PaymentCalculator()

    @Bean
    fun seatCodeParser(): DefaultSeatCodeParser = DefaultSeatCodeParser()

    private fun isLocal(): Boolean = customUrl() == null && dbProperties.local

    private fun customUrl(): String? = dbProperties.url
}
