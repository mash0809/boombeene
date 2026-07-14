package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.point.internal.entity.PointLedger;
import com.tonem.boombeene.point.internal.entity.PointType;
import com.tonem.boombeene.point.internal.entity.UserPoint;
import com.tonem.boombeene.point.internal.repository.PointLedgerRepository;
import com.tonem.boombeene.point.internal.repository.UserPointRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointLedgerRepository pointLedgerRepository;

    private PointService pointService;

    @BeforeEach
    void setUp() {
        pointService = new PointService(userPointRepository, pointLedgerRepository);
    }

    @Test
    void updateBalanceEarnsPointForNewUser() {
        String idempotencyKey = PointLedger.earnKey(10L, 100L);
        when(pointLedgerRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(userPointRepository.findByUserId(10L)).thenReturn(Optional.empty());

        pointService.updateBalance(10L, 100L);

        var userPointCaptor = ArgumentCaptor.forClass(UserPoint.class);
        verify(userPointRepository).save(userPointCaptor.capture());
        assertThat(userPointCaptor.getValue().getId()).isNull();
        assertThat(userPointCaptor.getValue().getUserId()).isEqualTo(10L);
        assertThat(userPointCaptor.getValue().getBalance()).isEqualTo(10);

        var ledgerCaptor = ArgumentCaptor.forClass(PointLedger.class);
        verify(pointLedgerRepository).save(ledgerCaptor.capture());
        assertThat(ledgerCaptor.getValue().getUserId()).isEqualTo(10L);
        assertThat(ledgerCaptor.getValue().getType()).isEqualTo(PointType.EARN);
        assertThat(ledgerCaptor.getValue().getAmount()).isEqualTo(10);
        assertThat(ledgerCaptor.getValue().getReportId()).isEqualTo(100L);
        assertThat(ledgerCaptor.getValue().getDescription()).isEqualTo("혼잡도 제보 적립");
        assertThat(ledgerCaptor.getValue().getIdempotencyKey()).isEqualTo(idempotencyKey);
    }

    @Test
    void updateBalanceAddsPointToExistingUser() {
        var userPoint = UserPoint.create(10L);
        String idempotencyKey = PointLedger.earnKey(10L, 100L);
        userPoint.addBalance(30);
        when(pointLedgerRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(userPointRepository.findByUserId(10L)).thenReturn(Optional.of(userPoint));

        pointService.updateBalance(10L, 100L);

        var userPointCaptor = ArgumentCaptor.forClass(UserPoint.class);
        verify(userPointRepository).save(userPointCaptor.capture());
        assertThat(userPointCaptor.getValue()).isSameAs(userPoint);
        assertThat(userPointCaptor.getValue().getBalance()).isEqualTo(40);

        verify(pointLedgerRepository).save(any(PointLedger.class));
    }

    @Test
    void updateBalanceSkipsWhenIdempotencyKeyAlreadyExists() {
        String idempotencyKey = PointLedger.earnKey(10L, 100L);
        when(pointLedgerRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

        pointService.updateBalance(10L, 100L);

        verifyNoInteractions(userPointRepository);
        verify(pointLedgerRepository, never()).save(any());
    }
}
