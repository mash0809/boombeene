package com.tonem.boombeene.crowdreport.dto;

import com.tonem.boombeene.crowdreport.entity.CongestionLevel;

public record CongestionResult(CongestionLevel level, int count, double distanceMeters) {

    public static CongestionResult none(double distanceMeters) {
        return new CongestionResult(null, 0, distanceMeters);
    }

    public static CongestionResult of(CongestionLevel level, int count, double distanceMeters) {
        return new CongestionResult(level, count, distanceMeters);
    }
}
