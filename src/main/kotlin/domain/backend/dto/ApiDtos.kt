package domain.backend.dto

import domain.model.payment.policy.PaymentMethod
import java.time.LocalDateTime

data class MoviesResponse(
    val movies: List<MovieResponse>,
)

data class MovieResponse(
    val id: Long,
    val title: String,
    val runningTimeMinutes: Long,
    val screenings: List<ScreeningResponse>,
)

data class ScreeningResponse(
    val id: Long,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
)

data class CreateReservationRequest(
    val reservations: List<ReservationItemRequest>,
    val usedPoints: Int,
    val paymentMethod: ApiPaymentMethod,
)

data class ReservationItemRequest(
    val screeningId: Long,
    val seats: List<String>,
)

data class CreateReservationResponse(
    val reservationId: Long,
    val reservations: List<ReservationItemResponse>,
    val usedPoints: Int,
    val paymentMethod: ApiPaymentMethod,
    val totalPrice: Int,
)

data class ReservationItemResponse(
    val screeningId: Long,
    val seats: List<String>,
)

enum class ApiPaymentMethod {
    CREDIT_CARD,
    CASH,
    ;

    fun toDomain(): PaymentMethod =
        when (this) {
            CREDIT_CARD -> PaymentMethod.CARD
            CASH -> PaymentMethod.CASH
        }
}
