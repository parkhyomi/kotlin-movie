package domain.backend

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.UUID

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class CinemaApiIntegrationTest(
    @param:LocalServerPort private val port: Int,
) {
    private lateinit var client: RestTestClient

    @BeforeEach
    fun setUp() {
        client =
            RestTestClient
                .bindToServer()
                .baseUrl("http://localhost:$port")
                .build()
    }

    @Test
    fun `영화 목록 조회 요청은 200과 영화 목록 JSON을 반환한다`() {
        client
            .get()
            .uri("/api/movies")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.movies")
            .isArray()
            .jsonPath("$.movies[0].id")
            .exists()
            .jsonPath("$.movies[0].title")
            .exists()
            .jsonPath("$.movies[0].runningTimeMinutes")
            .exists()
            .jsonPath("$.movies[0].screenings")
            .isArray()
    }

    @Test
    fun `예매 생성 요청은 201과 예매 응답을 반환한다`() {
        client
            .post()
            .uri("/api/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "reservations": [
                    {
                      "screeningId": 1,
                      "seats": ["C2", "C3"]
                    }
                  ],
                  "usedPoints": 2000,
                  "paymentMethod": "CREDIT_CARD"
                }
                """.trimIndent(),
            ).exchange()
            .expectStatus()
            .isCreated()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.reservationId")
            .exists()
            .jsonPath("$.reservations[0].screeningId")
            .isEqualTo(1)
            .jsonPath("$.reservations[0].seats[0]")
            .isEqualTo("C2")
            .jsonPath("$.usedPoints")
            .isEqualTo(2000)
            .jsonPath("$.paymentMethod")
            .isEqualTo("CREDIT_CARD")
            .jsonPath("$.totalPrice")
            .isEqualTo(24700)
    }

    @Test
    fun `이미 예약된 좌석을 예매하면 409를 반환한다`() {
        val body =
            """
            {
              "reservations": [
                {
                  "screeningId": 2,
                  "seats": ["A1"]
                }
              ],
              "usedPoints": 0,
              "paymentMethod": "CASH"
            }
            """.trimIndent()

        client
            .post()
            .uri("/api/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus()
            .isCreated()

        client
            .post()
            .uri("/api/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus()
            .isEqualTo(409)
            .expectBody()
            .jsonPath("$.message")
            .exists()
    }

    @Test
    fun `존재하지 않는 상영으로 예매 요청하면 404를 반환한다`() {
        client
            .post()
            .uri("/api/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "reservations": [
                    {
                      "screeningId": 999999,
                      "seats": ["A1"]
                    }
                  ],
                  "usedPoints": 0,
                  "paymentMethod": "CASH"
                }
                """.trimIndent(),
            ).exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .jsonPath("$.message")
            .exists()
    }

    @Test
    fun `잘못된 요청 형식이면 400을 반환한다`() {
        client
            .post()
            .uri("/api/reservations")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                """
                {
                  "reservations": [],
                  "usedPoints": 0,
                  "paymentMethod": "INVALID"
                }
                """.trimIndent(),
            ).exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody()
            .jsonPath("$.message")
            .exists()
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            val dbName = "cinema_http_test_${UUID.randomUUID().toString().replace("-", "")}"
            registry.add("cinema.db.url") { "jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1" }
        }
    }
}
