package com.tonem.boombeene.crowdreport.dto;

import com.tonem.boombeene.crowdreport.entity.CongestionLevel;

public record CongestionResult(CongestionLevel level, boolean hasData) {

    public static CongestionResult none() {
        return new CongestionResult(null, false);
    }

    public static CongestionResult of(CongestionLevel level) {
        return new CongestionResult(level, true);
    }
}
