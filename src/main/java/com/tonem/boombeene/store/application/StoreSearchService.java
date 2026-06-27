package com.tonem.boombeene.store.application;

import com.tonem.boombeene.store.client.KakaoDocument;
import com.tonem.boombeene.store.client.KakaoLocalApiClient;
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
        List<KakaoDocument> documents = kakaoLocalApiClient.searchByCategory(
                request.latitude(), request.longitude(), request.radius(), request.category().getKakaoGroupCode());
        return storeService.upsertAll(documents, request.category());
    }
}
