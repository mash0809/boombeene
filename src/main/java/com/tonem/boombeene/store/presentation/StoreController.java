package com.tonem.boombeene.store.presentation;

import com.tonem.boombeene.store.application.StoreSearchService;
import com.tonem.boombeene.store.dto.NearbySearchRequest;
import com.tonem.boombeene.store.dto.StoreResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreSearchService storeSearchService;

    @PostMapping("/nearby")
    public List<StoreResponse> searchNearby(@Valid @RequestBody NearbySearchRequest request) {
        return storeSearchService.searchNearby(request).stream()
                .map(StoreResponse::from)
                .toList();
    }
}
