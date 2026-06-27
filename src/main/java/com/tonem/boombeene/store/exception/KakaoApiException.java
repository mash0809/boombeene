package com.tonem.boombeene.store.exception;

public class KakaoApiException extends RuntimeException {

    public KakaoApiException() {
        super("Failed to call Kakao Local API");
    }

    public KakaoApiException(Throwable cause) {
        super("Failed to call Kakao Local API", cause);
    }
}
