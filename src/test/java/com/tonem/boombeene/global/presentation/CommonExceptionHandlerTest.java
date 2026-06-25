package com.tonem.boombeene.global.presentation;

import com.tonem.boombeene.global.common.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommonExceptionHandlerTest {

    private final CommonExceptionHandler exceptionHandler = new CommonExceptionHandler();

    @Test
    void handleEntityNotFoundReturnsExceptionMessage() {
        var response = exceptionHandler.handleEntityNotFound(new EntityNotFoundException("User", 1L));

        assertThat(response.message()).isEqualTo("User not found: 1");
    }
}
