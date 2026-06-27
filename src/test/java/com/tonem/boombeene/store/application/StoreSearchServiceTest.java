package com.tonem.boombeene.store.application;

import com.tonem.boombeene.store.client.KakaoDocument;
import com.tonem.boombeene.store.client.KakaoLocalApiClient;
import com.tonem.boombeene.store.dto.NearbySearchRequest;
import com.tonem.boombeene.store.dto.StoreDto;
import com.tonem.boombeene.store.entity.StoreCategory;
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
    void searchNearbyCallsKakaoBeforeDelegatingStoreUpsert() {
        var request = new NearbySearchRequest(37.498095, 127.027583, 500, StoreCategory.RESTAURANT);
        var document = new KakaoDocument("12345", "테스트 식당", "127.027583", "37.498095");
        var storeDto = new StoreDto(1L, "테스트 식당", 37.498095, 127.027583, StoreCategory.RESTAURANT);
        when(kakaoLocalApiClient.searchByCategory(37.498095, 127.027583, 500, "FD6"))
                .thenReturn(List.of(document));
        when(storeService.upsertAll(List.of(document), StoreCategory.RESTAURANT)).thenReturn(List.of(storeDto));

        var result = storeSearchService.searchNearby(request);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("테스트 식당");
        verify(storeService).upsertAll(List.of(document), StoreCategory.RESTAURANT);
    }

    @Test
    void searchNearbyIsNotTransactionalBecauseKakaoCallMustRunOutsideTransaction() throws NoSuchMethodException {
        Method method = StoreSearchService.class.getDeclaredMethod("searchNearby", NearbySearchRequest.class);

        assertThat(method.isAnnotationPresent(Transactional.class)).isFalse();
    }
}
