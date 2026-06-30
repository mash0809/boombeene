package com.tonem.boombeene.store.application;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.store.api.StoreFacade;
import com.tonem.boombeene.store.api.StoreInfo;
import com.tonem.boombeene.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class StoreFacadeImpl implements StoreFacade {

    private final StoreRepository storeRepository;

    @Override
    @Transactional(readOnly = true)
    public StoreInfo getById(Long storeId) {
        return storeRepository.findById(storeId)
                .map(store -> new StoreInfo(store.getId(), store.getLatitude(), store.getLongitude()))
                .orElseThrow(() -> new EntityNotFoundException("Store", storeId));
    }
}
