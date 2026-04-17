package global.exception

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler(
    private val serviceExceptionHandler: ServiceExceptionHandler,
) {
    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(error: ServiceException): ResponseEntity<ErrorResponse> = toResponse(error)

    @ExceptionHandler(IllegalArgumentException::class, HttpMessageNotReadableException::class)
    fun handleBadRequest(error: Exception): ResponseEntity<ErrorResponse> =
        toResponse(serviceExceptionHandler.badRequest(error.message ?: "잘못된 요청입니다."))

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(error: Exception): ResponseEntity<ErrorResponse> = toResponse(serviceExceptionHandler.internalServerError())

    private fun toResponse(error: ServiceException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(error.errorCode.status)
            .body(
                ErrorResponse(
                    code = error.errorCode.name,
                    message = error.message ?: error.errorCode.defaultMessage,
                ),
            )
}
