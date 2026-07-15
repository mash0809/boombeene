package com.tonem.boombeene.store.internal.application;

import com.tonem.boombeene.store.internal.application.StoreService;
import com.tonem.boombeene.store.internal.dto.KakaoDocument;
import com.tonem.boombeene.store.internal.entity.Store;
import com.tonem.boombeene.store.internal.entity.StoreCategory;
import com.tonem.boombeene.store.internal.exception.KakaoApiException;
import com.tonem.boombeene.store.internal.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("캐시에 없는 신규 매장은 일괄 저장한다")
    void upsertAllBatchSavesNewStoresWhenNotCached() {
        var document = new KakaoDocument("12345", "테스트 식당", "127.027583", "37.498095");
        when(storeRepository.findByPlaceIdIn(List.of("12345"))).thenReturn(List.of());

        var result = storeService.upsertAll(List.of(document), StoreCategory.RESTAURANT);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("테스트 식당");
        assertThat(result.getFirst().category()).isEqualTo(StoreCategory.RESTAURANT);

        var savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(storeRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(1);
    }

    @Test
    @DisplayName("이미 존재하는 매장은 재저장 없이 정보만 갱신한다")
    void upsertAllUpdatesExistingStoreWithoutSavingAgain() {
        var document = new KakaoDocument("12345", "새 이름", "127.027583", "37.498095");
        var existingStore = Store.create("12345", "옛 이름", 37.0, 127.0, StoreCategory.CAFE);
        when(storeRepository.findByPlaceIdIn(List.of("12345"))).thenReturn(List.of(existingStore));

        var result = storeService.upsertAll(List.of(document), StoreCategory.RESTAURANT);

        assertThat(result.getFirst().name()).isEqualTo("새 이름");
        assertThat(result.getFirst().category()).isEqualTo(StoreCategory.RESTAURANT);
        verify(storeRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("검색 결과가 없으면 저장소 호출 없이 빈 리스트를 반환한다")
    void upsertAllReturnsEmptyListWithoutRepositoryCallsWhenNoDocuments() {
        var result = storeService.upsertAll(List.of(), StoreCategory.RESTAURANT);

        assertThat(result).isEmpty();
        verify(storeRepository, never()).findByPlaceIdIn(anyList());
        verify(storeRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("검색 결과 건수와 무관하게 조회/저장 저장소 호출은 각각 한 번만 수행한다")
    void upsertAllCallsRepositoriesOnceRegardlessOfResultCount() {
        var documents = List.of(
                new KakaoDocument("1", "가게1", "127.0", "37.0"),
                new KakaoDocument("2", "가게2", "127.1", "37.1"),
                new KakaoDocument("3", "가게3", "127.2", "37.2")
        );
        when(storeRepository.findByPlaceIdIn(anyList())).thenReturn(List.of());

        storeService.upsertAll(documents, StoreCategory.RESTAURANT);

        // 검색 결과가 여러 건이어도 조회/저장은 각각 한 번만 호출해 N+1을 피한다.
        verify(storeRepository, times(1)).findByPlaceIdIn(anyList());
        verify(storeRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("잘못된 좌표값은 KakaoApiException으로 감싸서 던진다")
    void upsertAllWrapsInvalidCoordinateAsKakaoApiException() {
        var document = new KakaoDocument("12345", "테스트 식당", "", "37.498095");
        when(storeRepository.findByPlaceIdIn(List.of("12345"))).thenReturn(List.of());

        assertThatThrownBy(() -> storeService.upsertAll(List.of(document), StoreCategory.RESTAURANT))
                .isInstanceOf(KakaoApiException.class);
    }

    @Test
    @DisplayName("upsertAll은 @Transactional이 적용되어 있다")
    void upsertAllIsTransactional() throws NoSuchMethodException {
        Method method = StoreService.class.getDeclaredMethod("upsertAll", List.class, StoreCategory.class);

        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }
}
