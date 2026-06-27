package com.tonem.boombeene.store.dto;

import com.tonem.boombeene.store.entity.Store;
import com.tonem.boombeene.store.entity.StoreCategory;

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
