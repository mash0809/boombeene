package com.tonem.boombeene.crowdreport.internal.application;

import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportDto;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportRequest;
import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import com.tonem.boombeene.crowdreport.internal.entity.CrowdReport;
import com.tonem.boombeene.crowdreport.CrowdReportCompleted;
import com.tonem.boombeene.crowdreport.internal.exception.CooldownActiveException;
import com.tonem.boombeene.crowdreport.internal.exception.LocationTooFarException;
import com.tonem.boombeene.crowdreport.internal.repository.CrowdReportRepository;
import com.tonem.boombeene.store.StoreApi;
import com.tonem.boombeene.store.StoreInfo;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrowdReportServiceTest {

    @Mock
    private StoreApi storeApi;

    @Mock
    private CrowdReportRepository crowdReportRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CrowdReportService crowdReportService;

    @Test
    void reportSavesCrowdReportAndPublishesCompletedEvent() {
        var request = new CrowdReportRequest(1L, 37.5662952, 126.9779451, 0.0, CongestionLevel.NORMAL);

        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5662952, 126.9779451));
        when(crowdReportRepository.existsByUserIdAndStoreIdAndCreatedAtAfter(eq(10L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(false);
        when(crowdReportRepository.save(any(CrowdReport.class))).thenAnswer(invocation -> {
            CrowdReport report = invocation.getArgument(0);
            ReflectionTestUtils.setField(report, "id", 99L);
            return report;
        });

        CrowdReportDto response = crowdReportService.report(10L, request);

        assertThat(response.reportId()).isEqualTo(99L);

        var reportCaptor = ArgumentCaptor.forClass(CrowdReport.class);
        verify(crowdReportRepository).save(reportCaptor.capture());
        assertThat(reportCaptor.getValue().getStoreId()).isEqualTo(1L);
        assertThat(reportCaptor.getValue().getUserId()).isEqualTo(10L);
        assertThat(reportCaptor.getValue().getLevel()).isEqualTo(CongestionLevel.NORMAL);

        var eventCaptor = ArgumentCaptor.forClass(CrowdReportCompleted.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue())
                .isEqualTo(new CrowdReportCompleted(99L, 10L, 1L));
    }

    @Test
    void reportThrowsWhenUserLocationIsTooFar() {
        var request = new CrowdReportRequest(1L, 37.5662952, 126.9779451, 0.0, CongestionLevel.NORMAL);
        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5657037, 126.9768616));

        assertThatThrownBy(() -> crowdReportService.report(10L, request))
                .isInstanceOf(LocationTooFarException.class);

        verify(crowdReportRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void reportThrowsWhenCooldownIsActive() {
        var request = new CrowdReportRequest(1L, 37.5662952, 126.9779451, 0.0, CongestionLevel.NORMAL);
        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5662952, 126.9779451));
        when(crowdReportRepository.existsByUserIdAndStoreIdAndCreatedAtAfter(eq(10L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> crowdReportService.report(10L, request))
                .isInstanceOf(CooldownActiveException.class);

        verify(crowdReportRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getCongestionReturnsMajorityLevel() {
        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5662952, 126.9779451));
        when(crowdReportRepository.findLevelsByStoreIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        CongestionLevel.CROWDED,
                        CongestionLevel.NORMAL,
                        CongestionLevel.CROWDED,
                        CongestionLevel.NORMAL,
                        CongestionLevel.COMFORTABLE
                ));

        var result = crowdReportService.getCongestion(1L, 37.5662952, 126.9779451);

        assertThat(result.count()).isEqualTo(5);
        // 가장 개수가 많으면서 level 의 priority 가 높은 순으로 정렬하여 추출
        assertThat(result.level()).isEqualTo(CongestionLevel.CROWDED);
        assertThat(result.distanceMeters()).isZero();
    }

    @Test
    void getCongestionReturnsNoDataWhenRecentReportsDoNotExist() {
        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5662952, 126.9779451));
        when(crowdReportRepository.findLevelsByStoreIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        var result = crowdReportService.getCongestion(1L, 37.5662952, 126.9779451);

        assertThat(result.count()).isZero();
        assertThat(result.level()).isNull();
        assertThat(result.distanceMeters()).isZero();
    }
}
