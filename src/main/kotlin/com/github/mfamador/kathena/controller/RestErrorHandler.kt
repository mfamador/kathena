package com.meltwater.fairhair.document.controller

import com.github.mfamador.kathena.exception.PersonNotFoundException
import com.github.mfamador.kathena.model.ApiError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestErrorHandler {
    val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun handleError(e: PersonNotFoundException): ApiError {
        val message = "Person '${e.personId}' not found"
        log.error(message, e)
        return ApiError(e.personId, message)
    }
}
