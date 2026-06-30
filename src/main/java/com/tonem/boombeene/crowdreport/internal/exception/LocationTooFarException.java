package com.tonem.boombeene.crowdreport.internal.exception;

public class LocationTooFarException extends RuntimeException {

    public LocationTooFarException() {
        super("현재 위치가 가게와 너무 멀어 제보할 수 없습니다.");
    }
}
