package com.tonem.boombeene.store.internal.presentation;

import com.tonem.boombeene.store.internal.exception.KakaoApiException;
import com.tonem.boombeene.store.internal.presentation.StoreExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StoreExceptionHandlerTest {

    private final StoreExceptionHandler exceptionHandler = new StoreExceptionHandler();

    @Test
    @DisplayName("KakaoApiException 발생 시 예외 메시지를 응답으로 반환한다")
    void handleKakaoApiExceptionReturnsExceptionMessage() {
        var response = exceptionHandler.handleKakaoApiException(new KakaoApiException(new RuntimeException()));

        assertThat(response.message()).isEqualTo("Failed to call Kakao Local API");
    }
}
