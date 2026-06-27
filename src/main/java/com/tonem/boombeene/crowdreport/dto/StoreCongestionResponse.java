package com.tonem.boombeene.crowdreport.dto;

import com.tonem.boombeene.crowdreport.entity.CongestionLevel;

public record StoreCongestionResponse(Long storeId, CongestionLevel level, boolean hasData) {

    public static StoreCongestionResponse from(Long storeId, CongestionResult result) {
        return new StoreCongestionResponse(storeId, result.level(), result.hasData());
    }
}
