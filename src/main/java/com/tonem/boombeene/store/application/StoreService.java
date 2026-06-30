package com.tonem.boombeene.store.application;

import com.tonem.boombeene.store.dto.KakaoDocument;
import com.tonem.boombeene.store.dto.StoreDto;
import com.tonem.boombeene.store.entity.Store;
import com.tonem.boombeene.store.entity.StoreCategory;
import com.tonem.boombeene.store.exception.KakaoApiException;
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

    @Transactional
    public List<StoreDto> upsertAll(List<KakaoDocument> documents, StoreCategory category) {
        if (documents.isEmpty()) {
            return List.of();
        }

        List<String> placeIds = documents.stream().map(KakaoDocument::id).toList();
        Map<String, Store> existingStoresByPlaceId = storeRepository.findByPlaceIdIn(placeIds).stream()
                .collect(Collectors.toMap(Store::getPlaceId, Function.identity()));

        List<Store> newStores = new ArrayList<>();
        List<Store> allStores = new ArrayList<>();
        for (KakaoDocument document : documents) {
            double latitude = parseCoordinate(document.y());
            double longitude = parseCoordinate(document.x());

            Store store = existingStoresByPlaceId.get(document.id());
            if (store != null) {
                store.update(document.placeName(), latitude, longitude, category);
            } else {
                store = Store.create(document.id(), document.placeName(), latitude, longitude, category);
                newStores.add(store);
            }

            allStores.add(store);
        }

        if (!newStores.isEmpty()) {
            storeRepository.saveAll(newStores);
        }

        return allStores.stream().map(StoreDto::from).toList();
    }

    private double parseCoordinate(String coordinate) {
        try {
            return Double.parseDouble(coordinate);
        } catch (NumberFormatException exception) {
            throw new KakaoApiException(exception);
        }
    }
}
