package com.tonem.boombeene.store.internal.infra;

import com.tonem.boombeene.store.internal.dto.KakaoCategorySearchResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface KakaoLocalApiClient {

    @GetExchange("/v2/local/search/category.json")
    KakaoCategorySearchResponse searchByCategory(
            @RequestParam("y") double latitude,
            @RequestParam("x") double longitude,
            @RequestParam("radius") int radius,
            @RequestParam("category_group_code") String categoryGroupCode
    );
}
