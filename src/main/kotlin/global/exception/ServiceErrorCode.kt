package global.exception

import org.springframework.http.HttpStatus

enum class ServiceErrorCode(
    val status: HttpStatus,
    val defaultMessage: String,
) {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    SCREENING_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상영을 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "이미 예약된 좌석입니다."),
    OVERLAPPING_SCREENING(HttpStatus.CONFLICT, "겹치는 상영 시간은 예매할 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
}
