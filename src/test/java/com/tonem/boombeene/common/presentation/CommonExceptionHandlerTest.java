package com.tonem.boombeene.common.presentation;

import com.tonem.boombeene.common.exception.CommonExceptionHandler;
import com.tonem.boombeene.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class CommonExceptionHandlerTest {

    private final CommonExceptionHandler exceptionHandler = new CommonExceptionHandler();

    @Test
    void handleEntityNotFoundReturnsExceptionMessage() {
        var response = exceptionHandler.handleEntityNotFound(new EntityNotFoundException("User", 1L));

        assertThat(response.message()).isEqualTo("User not found: 1");
    }

    @Test
    void handleMethodArgumentNotValidReturnsErrorResponse() throws NoSuchMethodException {
        var target = new ValidationTarget();
        var bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "radius", "must be greater than or equal to 1"));
        Method method = ValidationTarget.class.getDeclaredMethod("validate", String.class);
        var exception = new MethodArgumentNotValidException(new MethodParameter(method, 0), bindingResult);

        var response = exceptionHandler.handleMethodArgumentNotValid(exception);

        assertThat(response.message()).isEqualTo("Invalid request");
    }

    private static class ValidationTarget {

        void validate(String value) {
        }
    }
}
