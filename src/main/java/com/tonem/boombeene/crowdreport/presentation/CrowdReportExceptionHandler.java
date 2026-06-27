package com.tonem.boombeene.crowdreport.presentation;

import com.tonem.boombeene.crowdreport.exception.CooldownActiveException;
import com.tonem.boombeene.crowdreport.exception.LocationTooFarException;
import com.tonem.boombeene.global.common.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.tonem.boombeene.crowdreport")
public class CrowdReportExceptionHandler {

    @ExceptionHandler(LocationTooFarException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    ErrorResponse handleLocationTooFarException(LocationTooFarException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(CooldownActiveException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    ErrorResponse handleCooldownActiveException(CooldownActiveException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}
