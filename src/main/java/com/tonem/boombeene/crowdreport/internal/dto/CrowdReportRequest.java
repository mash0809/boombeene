package com.tonem.boombeene.crowdreport.internal.dto;

import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CrowdReportRequest(
        @NotNull Long storeId,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @NotNull @DecimalMin("0") Double gpsAccuracy,
        @NotNull CongestionLevel level
) {
}
