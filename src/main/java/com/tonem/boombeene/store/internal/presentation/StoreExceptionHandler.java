package com.tonem.boombeene.store.internal.presentation;

import com.tonem.boombeene.common.exception.ErrorResponse;
import com.tonem.boombeene.store.internal.exception.KakaoApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.tonem.boombeene.store")
public class StoreExceptionHandler {

    @ExceptionHandler(KakaoApiException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    ErrorResponse handleKakaoApiException(KakaoApiException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}
