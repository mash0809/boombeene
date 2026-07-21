package com.tonem.boombeene.store.internal.application;

import com.tonem.boombeene.store.internal.dto.KakaoCategorySearchResponse;
import com.tonem.boombeene.store.internal.dto.KakaoDocument;
import com.tonem.boombeene.store.internal.dto.KakaoMeta;
import com.tonem.boombeene.store.internal.dto.NearbySearchRequest;
import com.tonem.boombeene.store.internal.dto.StoreDto;
import com.tonem.boombeene.store.internal.entity.StoreCategory;
import com.tonem.boombeene.store.internal.infra.KakaoLocalApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreSearchServiceTest {

    @Mock
    private KakaoLocalApiClient kakaoLocalApiClient;

    @Mock
    private StoreService storeService;

    @InjectMocks
    private StoreSearchService storeSearchService;

    @Test
    @DisplayName("주변 검색 시 카카오 API를 호출한 뒤 매장 upsert를 위임한다")
    void searchNearbyCallsKakaoBeforeDelegatingStoreUpsert() {
        var request = new NearbySearchRequest(37.498095, 127.027583, 500, StoreCategory.RESTAURANT);
        var document = new KakaoDocument("12345", "테스트 식당", "127.027583", "37.498095");
        var storeDto = new StoreDto(1L, "테스트 식당", 37.498095, 127.027583, StoreCategory.RESTAURANT);
        when(kakaoLocalApiClient.searchByCategory(37.498095, 127.027583, 500, "FD6", 1, 15))
                .thenReturn(new KakaoCategorySearchResponse(new KakaoMeta(true), List.of(document)));
        when(storeService.upsertAll(List.of(document), StoreCategory.RESTAURANT)).thenReturn(List.of(storeDto));

        var result = storeSearchService.searchNearby(request);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("테스트 식당");
        verify(storeService).upsertAll(List.of(document), StoreCategory.RESTAURANT);
    }

    @Test
    @DisplayName("카카오 API의 마지막 페이지까지 조회한 뒤 모든 매장을 upsert한다")
    void searchNearbyFetchesAllPagesBeforeDelegatingStoreUpsert() {
        var request = new NearbySearchRequest(37.498095, 127.027583, 500, StoreCategory.RESTAURANT);
        var firstDocument = new KakaoDocument("1", "첫 번째 식당", "127.0", "37.0");
        var secondDocument = new KakaoDocument("2", "두 번째 식당", "127.1", "37.1");
        var thirdDocument = new KakaoDocument("3", "세 번째 식당", "127.2", "37.2");
        var documents = List.of(firstDocument, secondDocument, thirdDocument);
        var storeDtos = List.of(
                new StoreDto(1L, "첫 번째 식당", 37.0, 127.0, StoreCategory.RESTAURANT),
                new StoreDto(2L, "두 번째 식당", 37.1, 127.1, StoreCategory.RESTAURANT),
                new StoreDto(3L, "세 번째 식당", 37.2, 127.2, StoreCategory.RESTAURANT));
        when(kakaoLocalApiClient.searchByCategory(37.498095, 127.027583, 500, "FD6", 1, 15))
                .thenReturn(new KakaoCategorySearchResponse(new KakaoMeta(false), List.of(firstDocument)));
        when(kakaoLocalApiClient.searchByCategory(37.498095, 127.027583, 500, "FD6", 2, 15))
                .thenReturn(new KakaoCategorySearchResponse(new KakaoMeta(false), List.of(secondDocument)));
        when(kakaoLocalApiClient.searchByCategory(37.498095, 127.027583, 500, "FD6", 3, 15))
                .thenReturn(new KakaoCategorySearchResponse(new KakaoMeta(true), List.of(thirdDocument)));
        when(storeService.upsertAll(documents, StoreCategory.RESTAURANT)).thenReturn(storeDtos);

        var result = storeSearchService.searchNearby(request);

        assertThat(result).containsExactlyElementsOf(storeDtos);
        verify(storeService).upsertAll(documents, StoreCategory.RESTAURANT);
    }

    @Test
    @DisplayName("카카오 API 호출이 트랜잭션 밖에서 실행되도록 searchNearby는 @Transactional이 아니다")
    void searchNearbyIsNotTransactionalBecauseKakaoCallMustRunOutsideTransaction() throws NoSuchMethodException {
        Method method = StoreSearchService.class.getDeclaredMethod("searchNearby", NearbySearchRequest.class);

        assertThat(method.isAnnotationPresent(Transactional.class)).isFalse();
    }
}
