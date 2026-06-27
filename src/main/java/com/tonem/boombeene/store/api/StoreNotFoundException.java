package com.tonem.boombeene.store.api;

public class StoreNotFoundException extends RuntimeException {

    public StoreNotFoundException(Long storeId) {
        super("Store not found: " + storeId);
    }
}
