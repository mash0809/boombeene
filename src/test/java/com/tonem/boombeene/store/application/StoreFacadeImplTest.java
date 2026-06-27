package com.tonem.boombeene.store.application;

import com.tonem.boombeene.global.common.EntityNotFoundException;
import com.tonem.boombeene.store.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreFacadeImplTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreFacadeImpl storeFacade;

    @Test
    void getByIdThrowsEntityNotFoundExceptionWhenStoreDoesNotExist() {
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeFacade.getById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
