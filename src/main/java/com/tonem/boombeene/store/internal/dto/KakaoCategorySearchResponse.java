package com.tonem.boombeene.store.internal.dto;

import java.util.List;

public record KakaoCategorySearchResponse(KakaoMeta meta, List<KakaoDocument> documents) {
}
