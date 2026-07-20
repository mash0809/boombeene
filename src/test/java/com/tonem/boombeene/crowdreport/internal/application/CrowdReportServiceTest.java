package com.tonem.boombeene.crowdreport.internal.application;

import com.tonem.boombeene.common.exception.EntityNotFoundException;
import com.tonem.boombeene.crowdreport.internal.dto.CongestionSample;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    private CrowdReportCooldownMarker cooldownMarker;

    @InjectMocks
    private CrowdReportService crowdReportService;

    @Test
    @DisplayName("제보를 저장하고 완료 이벤트를 발행한다")
    void reportSavesCrowdReportAndPublishesCompletedEvent() {
        var request = new CrowdReportRequest(
                1L,
                37.5662952,
                126.9779451,
                0.0,
                CongestionLevel.NORMAL,
                "좌석이 여유로워요"
        );

        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5662952, 126.9779451));
        when(cooldownMarker.tryMark(10L, 1L)).thenReturn(true);
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
        assertThat(reportCaptor.getValue().getComment()).isEqualTo("좌석이 여유로워요");

        var eventCaptor = ArgumentCaptor.forClass(CrowdReportCompleted.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue())
                .isEqualTo(new CrowdReportCompleted(99L, 10L, 1L));
    }

    @Test
    @DisplayName("사용자 위치가 매장과 너무 멀면 예외를 던진다")
    void reportThrowsWhenUserLocationIsTooFar() {
        var request = new CrowdReportRequest(1L, 37.5662952, 126.9779451, 0.0, CongestionLevel.NORMAL, null);
        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5657037, 126.9768616));

        assertThatThrownBy(() -> crowdReportService.report(10L, request))
                .isInstanceOf(LocationTooFarException.class);

        verify(cooldownMarker, never()).tryMark(anyLong(), anyLong());
        verify(crowdReportRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("쿨다운이 활성화된 상태면 예외를 던진다")
    void reportThrowsWhenCooldownIsActive() {
        var request = new CrowdReportRequest(1L, 37.5662952, 126.9779451, 0.0, CongestionLevel.NORMAL, null);
        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5662952, 126.9779451));
        when(cooldownMarker.tryMark(10L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> crowdReportService.report(10L, request))
                .isInstanceOf(CooldownActiveException.class);

        verify(crowdReportRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("저장에 실패하면 쿨다운을 취소한다")
    void reportCancelsCooldownWhenSaveFails() {
        var request = new CrowdReportRequest(1L, 37.5662952, 126.9779451, 0.0, CongestionLevel.NORMAL, null);
        var saveFailure = new RuntimeException("save failed");
        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5662952, 126.9779451));
        when(cooldownMarker.tryMark(10L, 1L)).thenReturn(true);
        when(crowdReportRepository.save(any(CrowdReport.class))).thenThrow(saveFailure);

        assertThatThrownBy(() -> crowdReportService.report(10L, request))
                .isSameAs(saveFailure);

        verify(cooldownMarker).cancel(10L, 1L);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("혼잡도 조회 시 다수를 차지하는 레벨을 반환한다")
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
        when(crowdReportRepository.findRecentComments(
                eq(1L),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(List.of("방금 자리가 났어요", "조금 붐벼요"));

        var result = crowdReportService.getCongestion(1L, 37.5662952, 126.9779451);

        assertThat(result.count()).isEqualTo(5);
        // 가장 개수가 많으면서 level 의 priority 가 높은 순으로 정렬하여 추출
        assertThat(result.level()).isEqualTo(CongestionLevel.CROWDED);
        assertThat(result.distanceMeters()).isZero();
        assertThat(result.comments()).containsExactly("방금 자리가 났어요", "조금 붐벼요");
    }

    @Test
    @DisplayName("최근 제보가 없으면 데이터 없음 상태를 반환한다")
    void getCongestionReturnsNoDataWhenRecentReportsDoNotExist() {
        when(storeApi.getById(1L)).thenReturn(new StoreInfo(1L, 37.5662952, 126.9779451));
        when(crowdReportRepository.findLevelsByStoreIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        var result = crowdReportService.getCongestion(1L, 37.5662952, 126.9779451);

        assertThat(result.count()).isZero();
        assertThat(result.level()).isNull();
        assertThat(result.distanceMeters()).isZero();
        assertThat(result.comments()).isEmpty();
        verify(crowdReportRepository, never()).findRecentComments(
                anyLong(),
                any(LocalDateTime.class),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("요일과 시간대별 평균 혼잡도와 표본 수를 반환한다")
    void getWeeklyCongestionReturnsAveragesAndCountsByDayAndHour() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate tuesday = monday.plusDays(1);
        when(crowdReportRepository.findByStoreIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        sample(CongestionLevel.CROWDED, monday.atTime(13, 10)),
                        sample(CongestionLevel.CROWDED, monday.atTime(13, 20)),
                        sample(CongestionLevel.NORMAL, monday.atTime(13, 30)),
                        sample(CongestionLevel.COMFORTABLE, monday.atTime(18, 0)),
                        sample(CongestionLevel.NORMAL, tuesday.atTime(13, 0))
                ));

        var result = crowdReportService.getWeeklyCongestion(1L);

        assertThat(result).hasSize(7);
        var mondayResult = result.get(DayOfWeek.MONDAY.ordinal());
        assertThat(mondayResult.day()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(mondayResult.level()).isEqualTo(CongestionLevel.NORMAL);
        assertThat(mondayResult.count()).isEqualTo(4);
        assertThat(mondayResult.hourly()).hasSize(24);
        assertThat(mondayResult.hourly().get(13).level()).isEqualTo(CongestionLevel.CROWDED);
        assertThat(mondayResult.hourly().get(13).count()).isEqualTo(3);
        assertThat(mondayResult.hourly().get(18).level()).isEqualTo(CongestionLevel.COMFORTABLE);
        assertThat(mondayResult.hourly().get(18).count()).isEqualTo(1);

        var tuesdayResult = result.get(DayOfWeek.TUESDAY.ordinal());
        assertThat(tuesdayResult.level()).isEqualTo(CongestionLevel.NORMAL);
        assertThat(tuesdayResult.count()).isEqualTo(1);
        assertThat(tuesdayResult.hourly().get(13).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("평균 혼잡도가 중간값이면 높은 레벨로 반올림한다")
    void getWeeklyCongestionRoundsHalfUp() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        when(crowdReportRepository.findByStoreIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        sample(CongestionLevel.COMFORTABLE, monday.atTime(10, 0)),
                        sample(CongestionLevel.NORMAL, monday.atTime(10, 30))
                ));

        var mondayResult = crowdReportService.getWeeklyCongestion(1L).get(DayOfWeek.MONDAY.ordinal());

        assertThat(mondayResult.level()).isEqualTo(CongestionLevel.NORMAL);
        assertThat(mondayResult.hourly().get(10).level()).isEqualTo(CongestionLevel.NORMAL);
    }

    @Test
    @DisplayName("제보가 없는 요일과 시간대는 데이터 없음 상태를 반환한다")
    void getWeeklyCongestionReturnsNoDataForEmptyDaysAndHours() {
        when(crowdReportRepository.findByStoreIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        var result = crowdReportService.getWeeklyCongestion(1L);

        assertThat(result).hasSize(7)
                .allSatisfy(day -> {
                    assertThat(day.level()).isNull();
                    assertThat(day.count()).isZero();
                    assertThat(day.hourly()).hasSize(24)
                            .allSatisfy(hour -> {
                                assertThat(hour.level()).isNull();
                                assertThat(hour.count()).isZero();
                            });
                });
    }

    @Test
    @DisplayName("주간 혼잡도 조회 시 28일 전 자정을 조회 기준으로 사용한다")
    void getWeeklyCongestionUsesTwentyEightDaysAgoAsCutoff() {
        when(crowdReportRepository.findByStoreIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());
        var cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        LocalDate expectedDate = LocalDate.now().minusDays(28);

        crowdReportService.getWeeklyCongestion(1L);

        verify(crowdReportRepository).findByStoreIdAndCreatedAtAfter(eq(1L), cutoffCaptor.capture());
        assertThat(cutoffCaptor.getValue()).isEqualTo(expectedDate.atStartOfDay());
    }

    @Test
    @DisplayName("존재하지 않는 매장의 주간 혼잡도를 조회하면 예외를 던진다")
    void getWeeklyCongestionThrowsWhenStoreDoesNotExist() {
        var exception = new EntityNotFoundException("Store", 1L);
        doThrow(exception).when(storeApi).validateExists(1L);

        assertThatThrownBy(() -> crowdReportService.getWeeklyCongestion(1L))
                .isSameAs(exception);

        verify(crowdReportRepository, never())
                .findByStoreIdAndCreatedAtAfter(anyLong(), any(LocalDateTime.class));
    }

    private CongestionSample sample(CongestionLevel level, LocalDateTime createdAt) {
        return new TestCongestionSample(level, createdAt);
    }

    private record TestCongestionSample(
            CongestionLevel level,
            LocalDateTime createdAt
    ) implements CongestionSample {

        @Override
        public CongestionLevel getLevel() {
            return level;
        }

        @Override
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
