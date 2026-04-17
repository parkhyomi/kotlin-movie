package controller

import domain.model.movie.Movie
import domain.model.payment.policy.PaymentMethod
import infra.db.SchemaInitializer
import infra.db.jdbc.JdbcMovieRepository
import infra.db.jdbc.JdbcReservationRepository
import infra.db.jdbc.JdbcScreeningRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class CinemaControllerTest {
    @Test
    fun `reserve는 좌석 코드를 공백 제거 후 대문자로 정규화해서 저장한다`() {
        val controller =
            controllerWithScreenings(
                screenings =
                    listOf(
                        ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 10), LocalTime.of(10, 0)),
                    ),
            )

        val item =
            controller.reserve(
                movieTitle = "탑건: 매버릭",
                date = LocalDate.of(2026, 4, 10),
                startTime = LocalTime.of(10, 0),
                seatCodes = listOf(" c2 ", "a12"),
            )

        assertThat(item.seats.map { seat -> "${seat.row.name}${seat.column}" }).containsExactly("C2", "A12")
    }

    @Test
    fun `reservationItems는 예약된 항목을 반환한다`() {
        val controller =
            controllerWithScreenings(
                screenings =
                    listOf(
                        ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 10), LocalTime.of(13, 0)),
                        ScreeningSeed("마더", LocalDate.of(2026, 4, 10), LocalTime.of(16, 0)),
                    ),
            )

        controller.reserve("탑건: 매버릭", LocalDate.of(2026, 4, 10), LocalTime.of(13, 0), listOf("C2", "C3"))
        controller.reserve("마더", LocalDate.of(2026, 4, 10), LocalTime.of(16, 0), listOf("E2"))

        val items = controller.reservationItems()

        assertAll(
            { assertThat(items).hasSize(2) },
            { assertThat(items[0].screening.movie.findMovieTitle()).isEqualTo("탑건: 매버릭") },
            { assertThat(items[0].seats.map { seat -> "${seat.row.name}${seat.column}" }).containsExactly("C2", "C3") },
            { assertThat(items[1].screening.movie.findMovieTitle()).isEqualTo("마더") },
            { assertThat(items[1].seats.map { seat -> "${seat.row.name}${seat.column}" }).containsExactly("E2") },
        )
    }

    @Test
    fun `이미 담긴 상영과 시간이 겹치면 hasOverlapping은 true를 반환한다`() {
        val date = LocalDate.of(2026, 4, 10)
        val controller =
            controllerWithScreenings(
                screenings =
                    listOf(
                        ScreeningSeed("탑건: 매버릭", date, LocalTime.of(10, 0)),
                        ScreeningSeed("마더", date, LocalTime.of(11, 30)),
                    ),
            )

        controller.reserve("탑건: 매버릭", date, LocalTime.of(10, 0), listOf("A1"))
        val candidate = controller.findScreenings("마더", date).first()

        assertThat(controller.hasOverlapping(candidate)).isTrue()
    }

    @Test
    fun `현재 샘플 입력 시나리오는 최종 결제 금액 38950원을 반환한다`() {
        val controller =
            controllerWithScreenings(
                screenings =
                    listOf(
                        ScreeningSeed("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0)),
                        ScreeningSeed("탑건: 매버릭", LocalDate.of(2026, 4, 7), LocalTime.of(10, 0)),
                        ScreeningSeed("체인소맨", LocalDate.of(2026, 4, 10), LocalTime.of(16, 0)),
                    ),
            )

        controller.reserve("마더", LocalDate.of(2026, 4, 6), LocalTime.of(13, 0), listOf("A9"))
        controller.reserve("탑건: 매버릭", LocalDate.of(2026, 4, 7), LocalTime.of(10, 0), listOf("E1"))
        controller.reserve("체인소맨", LocalDate.of(2026, 4, 10), LocalTime.of(16, 0), listOf("C8"))

        val result = controller.payAmountApply(point = 500, paymentMethod = PaymentMethod.CARD)

        assertThat(result).isEqualTo(38_950)
    }

    private fun controllerWithScreenings(screenings: List<ScreeningSeed>): CinemaController {
        val dbUrl = inMemoryUrl()
        SchemaInitializer.initializeWithUrl(dbUrl)

        val movieRepository = JdbcMovieRepository(isLocal = false, customUrl = dbUrl)
        movieRepository.saveAll(Movie.sampleMovies)

        val reservationRepository = JdbcReservationRepository(isLocal = false, customUrl = dbUrl)
        val screeningRepository =
            JdbcScreeningRepository(
                isLocal = false,
                customUrl = dbUrl,
                movieRepository = movieRepository,
                reservationRepository = reservationRepository,
            )

        screenings.forEach { seed ->
            screeningRepository.createScreening(
                movieTitle = seed.movieTitle,
                screeningDate = seed.screeningDate,
                startTime = seed.startTime,
            )
        }

        return CinemaController(
            screeningRepository = screeningRepository,
        )
    }

    private fun inMemoryUrl(): String {
        val testDbName = "cinema_controller_test_${UUID.randomUUID().toString().replace("-", "")}"
        return "jdbc:h2:mem:$testDbName;DB_CLOSE_DELAY=-1"
    }

    private data class ScreeningSeed(
        val movieTitle: String,
        val screeningDate: LocalDate,
        val startTime: LocalTime,
    )
}
