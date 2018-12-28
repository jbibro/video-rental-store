package com.github.jbibro.videorentalstore.rental

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.reactive.function.server.ServerResponse

data class VideoRentalException(val errorCode: ErrorCode, override val message: String? = null) : RuntimeException(message)

enum class ErrorCode(val code: Int, val httpCode: HttpStatus) {
    CUSTOMER_NOT_FOUND(1, NOT_FOUND),
    FILM_NOT_FOUND(2, NOT_FOUND),
    RENTAL_NOT_FOUND(3, NOT_FOUND),
    INVALID_RENTAL_PERIOD(4, BAD_REQUEST)
}

data class RentalErrorResponse(
    val error: ErrorCode,
    val errorCode: Int,
    val message: String?
)

fun VideoRentalException.toServerResponse() =
    ServerResponse
        .status(this.errorCode.httpCode)
        .syncBody(RentalErrorResponse(this.errorCode, this.errorCode.code, this.message))


inline fun <reified A> Throwable.asException(): A {
    return this as A
}