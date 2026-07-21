package com.tonem.boombeene.store.internal.application;

import com.tonem.boombeene.store.internal.dto.KakaoCategorySearchResponse;
import com.tonem.boombeene.store.internal.dto.KakaoDocument;
import com.tonem.boombeene.store.internal.dto.NearbySearchRequest;
import com.tonem.boombeene.store.internal.dto.StoreDto;
import com.tonem.boombeene.store.internal.exception.KakaoApiException;
import com.tonem.boombeene.store.internal.infra.KakaoLocalApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreSearchService {

    private static final int KAKAO_PAGE_SIZE = 15;
    // 카카오 카테고리 검색은 pageable_count 기준 최대 45개의 문서만 노출하므로 45 / 15 = 3
    private static final int KAKAO_MAX_RESULT_PAGE = 3;

    private final KakaoLocalApiClient kakaoLocalApiClient;
    private final StoreService storeService;

    public List<StoreDto> searchNearby(NearbySearchRequest request) {
        var documents = new ArrayList<KakaoDocument>();

        // kakao API를 계속 호출하지 않도록 노출 가능한 최대 페이지까지만 조회한다.
        for (int page = 1; page <= KAKAO_MAX_RESULT_PAGE; page++) {
            var response = kakaoLocalApiClient.searchByCategory(
                    request.latitude(), request.longitude(), request.radius(),
                    request.category().getKakaoGroupCode(), page, KAKAO_PAGE_SIZE);
            validateResponse(response);
            documents.addAll(response.documents());

            if (response.meta().isEnd()) {
                break;
            }
        }

        return storeService.upsertAll(documents, request.category());
    }

    private void validateResponse(KakaoCategorySearchResponse response) {
        if (response == null || response.meta() == null || response.meta().isEnd() == null
                || response.documents() == null) {
            throw new KakaoApiException(new IllegalStateException("Kakao Local API response is invalid"));
        }
    }
}
