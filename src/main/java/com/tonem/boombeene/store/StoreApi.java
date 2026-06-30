package com.tonem.boombeene.store;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.store.internal.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreApi {

    private final StoreRepository storeRepository;

    public StoreInfo getById(Long storeId) {
        return storeRepository.findById(storeId)
                .map(store -> new StoreInfo(store.getId(), store.getLatitude(), store.getLongitude()))
                .orElseThrow(() -> new EntityNotFoundException("Store", storeId));
    }
}
