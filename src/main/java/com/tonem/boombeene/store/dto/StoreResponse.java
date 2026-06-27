package com.tonem.boombeene.store.dto;

import com.tonem.boombeene.store.entity.StoreCategory;

public record StoreResponse(Long id, String name, Double latitude, Double longitude, StoreCategory category) {

    public static StoreResponse from(StoreDto store) {
        return new StoreResponse(store.id(), store.name(), store.latitude(), store.longitude(), store.category());
    }
}
