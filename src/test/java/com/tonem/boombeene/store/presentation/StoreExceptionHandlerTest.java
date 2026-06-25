package com.tonem.boombeene.store.presentation;

import com.tonem.boombeene.store.exception.KakaoApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoreExceptionHandlerTest {

    private final StoreExceptionHandler exceptionHandler = new StoreExceptionHandler();

    @Test
    void handleKakaoApiExceptionReturnsExceptionMessage() {
        var response = exceptionHandler.handleKakaoApiException(new KakaoApiException(new RuntimeException()));

        assertThat(response.message()).isEqualTo("Failed to call Kakao Local API");
    }
}
