package com.tonem.boombeene.crowdreport.internal.presentation;

import com.tonem.boombeene.crowdreport.internal.exception.CooldownActiveException;
import com.tonem.boombeene.crowdreport.internal.exception.LocationTooFarException;
import com.tonem.boombeene.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.tonem.boombeene.crowdreport")
public class CrowdReportExceptionHandler {

    @ExceptionHandler(LocationTooFarException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleLocationTooFarException(LocationTooFarException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler(CooldownActiveException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleCooldownActiveException(CooldownActiveException exception) {
        return new ErrorResponse(exception.getMessage());
    }
}
