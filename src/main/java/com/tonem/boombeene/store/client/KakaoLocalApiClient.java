package com.tonem.boombeene.store.client;

import com.tonem.boombeene.store.exception.KakaoApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoLocalApiClient {

    private final RestClient kakaoRestClient;

    // https://developers.kakao.com/docs/ko/local/dev-guide#search-by-category
    private final static String SEARCH_BY_CATEGORY_PATH = "/v2/local/search/category.json";

    public List<KakaoDocument> searchByCategory(
            double latitude,
            double longitude,
            int radius,
            String categoryGroupCode
    ) {
        try {
            KakaoCategorySearchResponse response = kakaoRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(SEARCH_BY_CATEGORY_PATH)
                            .queryParam("category_group_code", categoryGroupCode)
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("radius", radius)
                            .build())
                    .retrieve()
                    .body(KakaoCategorySearchResponse.class);

            if (response == null) {
                throw new KakaoApiException(new IllegalStateException("Kakao Local API response body is null"));
            }

            if (response.documents() == null) {
                throw new KakaoApiException(new IllegalStateException("Kakao Local API documents is null"));
            }

            return response.documents();
        } catch (RestClientException exception) {
            throw new KakaoApiException(exception);
        }
    }
}
