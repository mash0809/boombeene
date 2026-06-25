package com.tonem.boombeene.store.application;

import com.tonem.boombeene.store.client.KakaoDocument;
import com.tonem.boombeene.store.client.KakaoLocalApiClient;
import com.tonem.boombeene.store.dto.NearbySearchRequest;
import com.tonem.boombeene.store.dto.StoreDto;
import com.tonem.boombeene.store.entity.Store;
import com.tonem.boombeene.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final KakaoLocalApiClient kakaoLocalApiClient;

    @Transactional
    public List<StoreDto> searchNearby(NearbySearchRequest request) {
        List<KakaoDocument> documents = kakaoLocalApiClient.searchByCategory(
                request.latitude(), request.longitude(), request.radius(), request.category().getKakaoGroupCode());
        if (documents.isEmpty()) {
            return List.of();
        }

        List<String> placeIds = documents.stream().map(KakaoDocument::id).toList();
        Map<String, Store> existingStoresByPlaceId = storeRepository.findByPlaceIdIn(placeIds).stream()
                .collect(Collectors.toMap(Store::getPlaceId, Function.identity()));

        List<Store> newStores = new ArrayList<>();
        List<Store> stores = new ArrayList<>();
        for (KakaoDocument document : documents) {
            double latitude = Double.parseDouble(document.y());
            double longitude = Double.parseDouble(document.x());
            Store store = existingStoresByPlaceId.get(document.id());
            if (store != null) {
                store.updateLocation(document.placeName(), latitude, longitude);
            } else {
                store = Store.create(document.id(), document.placeName(), latitude, longitude, request.category());
                newStores.add(store);
            }
            stores.add(store);
        }

        if (!newStores.isEmpty()) {
            storeRepository.saveAll(newStores);
        }

        return stores.stream().map(StoreDto::from).toList();
    }
}
