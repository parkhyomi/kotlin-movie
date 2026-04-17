package domain.backend.service

import domain.backend.dto.CreateReservationRequest
import domain.backend.dto.CreateReservationResponse
import domain.backend.dto.ReservationItemResponse
import domain.backend.parser.DefaultSeatCodeParser
import domain.backend.repository.ReservationRepository
import domain.backend.repository.ScreeningCatalogQueryRepository
import domain.model.cart.Cart
import domain.model.cart.CartItem
import domain.model.payment.PaymentCalculator
import domain.model.seat.Seat
import global.exception.ServiceExceptionHandler
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class ReservationApiService(
    private val screeningCatalogQueryRepository: ScreeningCatalogQueryRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentCalculator: PaymentCalculator,
    private val seatCodeParser: DefaultSeatCodeParser,
    private val serviceExceptionHandler: ServiceExceptionHandler,
) {
    // 데모 목적의 응답용 ID(예매 단위). 좌석 예약 데이터 자체는 기존 reservation 테이블에 저장된다.
    private val reservationIdSequence = AtomicLong(0)

    fun createReservation(request: CreateReservationRequest): CreateReservationResponse {
        // 1) HTTP 입력(JSON)을 기존 도메인 Seat로 변환 + 기본 검증
        if (request.reservations.isEmpty()) {
            throw serviceExceptionHandler.badRequest("예매 항목은 1개 이상이어야 합니다.")
        }

        val normalizedItems =
            request.reservations.map { item ->
                val seats = seatCodeParser.parse(item.seats)
                if (seats.isEmpty()) {
                    throw serviceExceptionHandler.badRequest("좌석을 1개 이상 입력해 주세요.")
                }
                NormalizedReservationItem(
                    screeningId = item.screeningId,
                    seats = seats,
                )
            }

        // 2) 같은 상영관(screeningId) 단위로 좌석을 합친다.
        val seatsByScreening = linkedMapOf<Long, MutableList<Seat>>()
        normalizedItems.forEach { item ->
            seatsByScreening.getOrPut(item.screeningId) { mutableListOf() }.addAll(item.seats)
        }

        // 3) 한 요청 안에서 중복 좌석 차단
        seatsByScreening.forEach { (_, seats) ->
            if (seats.distinct().size != seats.size) {
                throw serviceExceptionHandler.badRequest("중복 좌석은 예약할 수 없습니다.")
            }
        }

        // 4) 기존 Cart 도메인으로 "시간 겹침" 규칙 검증
        val cart = Cart()
        seatsByScreening.forEach { (screeningId, seats) ->
            val screening =
                screeningCatalogQueryRepository.findScreeningById(screeningId)
                    ?: throw serviceExceptionHandler.screeningNotFound(screeningId)

            try {
                cart.add(
                    CartItem(
                        screening = screening,
                        seats = seats.toList(),
                    ),
                )
            } catch (_: IllegalArgumentException) {
                throw serviceExceptionHandler.overlappingScreening()
            }
        }

        // 5) 현재 DB 상태를 보고 이미 예약된 좌석이면 충돌 처리
        seatsByScreening.forEach { (screeningId, seats) ->
            val reservedSeats = reservationRepository.findReservedSeats(screeningId).toSet()
            if (seats.any { seat -> reservedSeats.contains(seat) }) {
                throw serviceExceptionHandler.seatAlreadyReserved()
            }
        }

        // 6) 예약 저장 (기존 reservation repository 사용)
        seatsByScreening.forEach { (screeningId, seats) ->
            try {
                reservationRepository.reserveSeats(screeningId, seats)
            } catch (error: IllegalArgumentException) {
                if (error.message?.contains("이미 예약된 좌석") == true) {
                    throw serviceExceptionHandler.seatAlreadyReserved()
                }
                throw error
            }
        }

        // 7) 기존 결제 도메인으로 총 결제 금액 계산
        val totalPrice =
            paymentCalculator.calculate(
                items = cart.items(),
                point = request.usedPoints,
                paymentMethod = request.paymentMethod.toDomain(),
            )

        return CreateReservationResponse(
            reservationId = reservationIdSequence.incrementAndGet(),
            reservations =
                normalizedItems.map { item ->
                    ReservationItemResponse(
                        screeningId = item.screeningId,
                        seats =
                            item.seats.map { seat ->
                                seatCodeOf(seat)
                            },
                    )
                },
            usedPoints = request.usedPoints,
            paymentMethod = request.paymentMethod,
            totalPrice = totalPrice,
        )
    }

    private fun seatCodeOf(seat: Seat): String = "${seat.row.name}${seat.column}"

    private data class NormalizedReservationItem(
        val screeningId: Long,
        val seats: List<Seat>,
    )
}
