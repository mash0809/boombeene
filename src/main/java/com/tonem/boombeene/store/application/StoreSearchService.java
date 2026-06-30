package com.tonem.boombeene.store.application;

import com.tonem.boombeene.store.exception.KakaoApiException;
import com.tonem.boombeene.store.infra.KakaoLocalApiClient;
import com.tonem.boombeene.store.dto.NearbySearchRequest;
import com.tonem.boombeene.store.dto.StoreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreSearchService {

    private final KakaoLocalApiClient kakaoLocalApiClient;
    private final StoreService storeService;

    public List<StoreDto> searchNearby(NearbySearchRequest request) {
        var response = kakaoLocalApiClient.searchByCategory(
                request.latitude(), request.longitude(), request.radius(), request.category().getKakaoGroupCode());
        if (response.documents() == null) {
            throw new KakaoApiException(new IllegalStateException("Kakao Local API documents is null"));
        }
        return storeService.upsertAll(response.documents(), request.category());
    }
}
