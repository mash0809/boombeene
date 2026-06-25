package com.tonem.boombeene.global.presentation;

import com.tonem.boombeene.global.common.ErrorResponse;
import com.tonem.boombeene.global.common.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse handleEntityNotFound(EntityNotFoundException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}
