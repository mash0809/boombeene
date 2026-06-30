package com.tonem.boombeene.store.internal.entity;

import lombok.Getter;

@Getter
public enum StoreCategory {
    RESTAURANT("FD6"),
    CAFE("CE7");

    private final String kakaoGroupCode;

    StoreCategory(String kakaoGroupCode) {
        this.kakaoGroupCode = kakaoGroupCode;
    }

}
