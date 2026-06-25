package com.tonem.boombeene.store.presentation;

import com.tonem.boombeene.store.application.StoreService;
import com.tonem.boombeene.store.dto.NearbySearchRequest;
import com.tonem.boombeene.store.dto.StoreResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/nearby")
    public List<StoreResponse> searchNearby(@Valid @ModelAttribute NearbySearchRequest request) {
        return storeService.searchNearby(request).stream()
                .map(StoreResponse::from)
                .toList();
    }
}
