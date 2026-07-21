package com.tonem.boombeene.crowdreport.internal.entity;

import java.util.Arrays;
import lombok.Getter;

// priority: 혼잡도 강도를 나타냄 (개수가 동일하면 더 큰 priority 채택)
@Getter
public enum CongestionLevel {
    COMFORTABLE(0),
    NORMAL(1),
    CROWDED(2);

    private final int priority;

    CongestionLevel(int priority) {
        this.priority = priority;
    }

    public static CongestionLevel fromPriority(int priority) {
        return Arrays.stream(values())
                .filter(level -> level.priority == priority)
                .findFirst()
                .orElseThrow();
    }
}
