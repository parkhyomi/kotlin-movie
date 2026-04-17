package domain.backend.controller

import domain.backend.dto.CreateReservationRequest
import domain.backend.dto.CreateReservationResponse
import domain.backend.dto.MoviesResponse
import domain.backend.service.MovieApiService
import domain.backend.service.ReservationApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class CinemaApiController(
    private val movieApiService: MovieApiService,
    private val reservationApiService: ReservationApiService,
) {
    // 영화/상영 조회: 읽기 전용 API
    @GetMapping("/movies")
    fun movies(): MoviesResponse =
        MoviesResponse(
            movies = movieApiService.findMovies(),
        )

    // 예매 생성: 도메인 규칙 검증 후 DB 저장
    @PostMapping("/reservations")
    fun createReservation(
        @RequestBody request: CreateReservationRequest,
    ): ResponseEntity<CreateReservationResponse> {
        val response = reservationApiService.createReservation(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
