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

    public List<KakaoDocument> searchByCategory(double latitude, double longitude, int radius, String categoryGroupCode) {
        try {
            KakaoCategorySearchResponse response = kakaoRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/category.json")
                            .queryParam("category_group_code", categoryGroupCode)
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("radius", radius)
                            .build())
                    .retrieve()
                    .body(KakaoCategorySearchResponse.class);
            if (response == null || response.documents() == null) {
                throw new KakaoApiException();
            }
            return response.documents();
        } catch (RestClientException exception) {
            throw new KakaoApiException(exception);
        }
    }
}
