package global.exception

import org.springframework.stereotype.Component

@Component
class ServiceExceptionHandler {
    fun badRequest(message: String): ServiceException =
        ServiceException(
            errorCode = ServiceErrorCode.BAD_REQUEST,
            message = message,
        )

    fun screeningNotFound(screeningId: Long): ServiceException =
        ServiceException(
            errorCode = ServiceErrorCode.SCREENING_NOT_FOUND,
            message = "해당 상영을 찾을 수 없습니다. screeningId=$screeningId",
        )

    fun seatAlreadyReserved(): ServiceException =
        ServiceException(
            errorCode = ServiceErrorCode.SEAT_ALREADY_RESERVED,
        )

    fun overlappingScreening(): ServiceException =
        ServiceException(
            errorCode = ServiceErrorCode.OVERLAPPING_SCREENING,
        )

    fun internalServerError(): ServiceException =
        ServiceException(
            errorCode = ServiceErrorCode.INTERNAL_SERVER_ERROR,
        )
}
