package com.tonem.boombeene.crowdreport.internal.dto;

import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import java.util.List;

public record CongestionResult(CongestionLevel level, int count, double distanceMeters, List<String> comments) {

    public static CongestionResult none(double distanceMeters) {
        return new CongestionResult(null, 0, distanceMeters, List.of());
    }

    public static CongestionResult of(
            CongestionLevel level,
            int count,
            double distanceMeters,
            List<String> comments
    ) {
        return new CongestionResult(level, count, distanceMeters, comments);
    }
}
