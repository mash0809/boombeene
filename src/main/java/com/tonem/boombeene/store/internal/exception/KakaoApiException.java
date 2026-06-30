package com.tonem.boombeene.store.internal.exception;

public class KakaoApiException extends RuntimeException {

    public KakaoApiException() {
        super("Failed to call Kakao Local API");
    }

    public KakaoApiException(Throwable cause) {
        super("Failed to call Kakao Local API", cause);
    }
}
