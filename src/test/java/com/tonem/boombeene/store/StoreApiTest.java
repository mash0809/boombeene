package com.tonem.boombeene.store;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.store.internal.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreApiTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreApi storeApi;

    @Test
    void getByIdThrowsEntityNotFoundExceptionWhenStoreDoesNotExist() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeApi.getById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
