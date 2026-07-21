package com.tonem.boombeene.crowdreport.internal.dto;

import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import java.time.DayOfWeek;
import java.util.List;

public record WeeklyCongestionResult(
        DayOfWeek day,
        CongestionLevel level,
        int count,
        List<HourlyCongestion> hourly
) {
}
