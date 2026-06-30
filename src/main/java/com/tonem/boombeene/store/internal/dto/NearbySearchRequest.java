package com.tonem.boombeene.store.internal.dto;

import com.tonem.boombeene.store.internal.entity.StoreCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NearbySearchRequest(
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @NotNull @Min(1) @Max(20000) Integer radius,
        @NotNull StoreCategory category
) {
}
