package com.tonem.boombeene.crowdreport.exception;

public class CooldownActiveException extends RuntimeException {

    public CooldownActiveException() {
        super("같은 가게는 30분 후에 다시 제보할 수 있습니다.");
    }
}
