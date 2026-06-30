package com.tonem.boombeene.crowdreport.internal.dto;

import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;

public record StoreCongestionResponse(Long storeId, CongestionLevel level, int count, double distanceMeters) {

    public static StoreCongestionResponse from(Long storeId, CongestionResult result) {
        return new StoreCongestionResponse(storeId, result.level(), result.count(), result.distanceMeters());
    }
}
