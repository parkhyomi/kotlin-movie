package global.exception

class ServiceException(
    val errorCode: ServiceErrorCode,
    message: String = errorCode.defaultMessage,
) : RuntimeException(message)
