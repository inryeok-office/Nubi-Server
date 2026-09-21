package com.inryeokoffice.nubi.api

import com.inryeokoffice.nubi.external.gwangju.GwangjuExternalApiException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant

class ResourceNotFoundException(
    message: String,
) : RuntimeException(message)

class InvalidRequestException(
    message: String,
) : RuntimeException(message)

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(exception: ResourceNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse("NOT_FOUND", exception.message.orEmpty()))

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequest(exception: InvalidRequestException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse("INVALID_REQUEST", exception.message.orEmpty()))

    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class,
    )
    fun handleParameterError(exception: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(ErrorResponse("INVALID_REQUEST", exception.message.orEmpty()))

    @ExceptionHandler(GwangjuExternalApiException::class)
    fun handleExternalApi(exception: GwangjuExternalApiException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ErrorResponse("UPSTREAM_ERROR", exception.message.orEmpty()))
}
