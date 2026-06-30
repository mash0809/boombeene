package com.tonem.boombeene.store.internal.dto;

import com.tonem.boombeene.store.internal.entity.Store;
import com.tonem.boombeene.store.internal.entity.StoreCategory;

public record StoreDto(Long id, String name, Double latitude, Double longitude, StoreCategory category) {

    public static StoreDto from(Store store) {
        return new StoreDto(
                store.getId(),
                store.getName(),
                store.getLatitude(),
                store.getLongitude(),
                store.getCategory()
        );
    }
}
