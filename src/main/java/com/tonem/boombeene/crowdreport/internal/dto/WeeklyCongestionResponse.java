package com.tonem.boombeene.crowdreport.internal.dto;

import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import java.time.DayOfWeek;
import java.util.List;

public record WeeklyCongestionResponse(
        Long storeId,
        DayOfWeek day,
        CongestionLevel level,
        int count,
        List<HourlyCongestion> hourly
) {

    public static WeeklyCongestionResponse from(Long storeId, WeeklyCongestionResult result) {
        return new WeeklyCongestionResponse(storeId, result.day(), result.level(), result.count(), result.hourly());
    }
}
