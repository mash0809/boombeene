package com.tonem.boombeene.store.client;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoDocument(String id, String placeName, String x, String y) {
}
