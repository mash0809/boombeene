package com.tonem.boombeene.crowdreport.internal.dto;

import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;

public record HourlyCongestion(int hour, CongestionLevel level, int count) {
}
