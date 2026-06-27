package com.tonem.boombeene.point.application;

import com.tonem.boombeene.crowdreport.event.CrowdReportCompleted;
import com.tonem.boombeene.point.entity.PointLedger;
import com.tonem.boombeene.point.entity.PointType;
import com.tonem.boombeene.point.entity.UserPoint;
import com.tonem.boombeene.point.repository.PointLedgerRepository;
import com.tonem.boombeene.point.repository.UserPointRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private PointService pointService;

    @Test
    void onEarnsPointForNewUser() {
        var event = new CrowdReportCompleted(100L, 10L, 1L);
        when(pointLedgerRepository.existsByIdempotencyKey("EARN_10_100")).thenReturn(false);
        when(userPointRepository.findByUserId(10L)).thenReturn(Optional.empty());

        pointService.on(event);

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
        assertThat(ledgerCaptor.getValue().getIdempotencyKey()).isEqualTo("EARN_10_100");
    }

    @Test
    void onAddsPointToExistingUser() {
        var event = new CrowdReportCompleted(100L, 10L, 1L);
        var userPoint = UserPoint.create(10L);
        userPoint.addBalance(30);
        when(pointLedgerRepository.existsByIdempotencyKey("EARN_10_100")).thenReturn(false);
        when(userPointRepository.findByUserId(10L)).thenReturn(Optional.of(userPoint));

        pointService.on(event);

        var userPointCaptor = ArgumentCaptor.forClass(UserPoint.class);
        verify(userPointRepository).save(userPointCaptor.capture());
        assertThat(userPointCaptor.getValue()).isSameAs(userPoint);
        assertThat(userPointCaptor.getValue().getBalance()).isEqualTo(40);

        verify(pointLedgerRepository).save(any(PointLedger.class));
    }

    @Test
    void onSkipsWhenIdempotencyKeyAlreadyExists() {
        var event = new CrowdReportCompleted(100L, 10L, 1L);
        when(pointLedgerRepository.existsByIdempotencyKey("EARN_10_100")).thenReturn(true);

        pointService.on(event);

        verifyNoInteractions(userPointRepository);
        verify(pointLedgerRepository, never()).save(any());
    }
}
