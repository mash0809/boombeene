package com.tonem.boombeene.crowdreport.internal.application;

import com.tonem.boombeene.crowdreport.internal.dto.CongestionResult;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportDto;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportRequest;
import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import com.tonem.boombeene.crowdreport.internal.entity.CrowdReport;
import com.tonem.boombeene.crowdreport.CrowdReportCompleted;
import com.tonem.boombeene.crowdreport.internal.exception.CooldownActiveException;
import com.tonem.boombeene.crowdreport.internal.exception.LocationTooFarException;
import com.tonem.boombeene.crowdreport.internal.repository.CrowdReportRepository;
import com.tonem.boombeene.crowdreport.internal.util.HaversineUtils;
import com.tonem.boombeene.store.StoreApi;
import com.tonem.boombeene.store.StoreInfo;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrowdReportService {

    private final StoreApi storeApi;
    private final CrowdReportRepository crowdReportRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CrowdReportCooldownMarker cooldownMarker;

    private final int COOLDOWN_MINUTES = 30;

    @Transactional
    public CrowdReportDto report(Long userId, CrowdReportRequest request) {
        StoreInfo store = storeApi.getById(request.storeId());
        boolean isWithinAllowedRadius = HaversineUtils.isWithinAllowedRadius(
                request.latitude(),
                request.longitude(),
                store.latitude(),
                store.longitude(),
                request.gpsAccuracy()
        );

        // 혼잡도 리포트 할 수 있는 거리가 아닌 경우 (허용 반경에서 벗어나있음)
        if (!isWithinAllowedRadius) {
            throw new LocationTooFarException();
        }

        // 조회와 마킹을 원자적으로 처리해 동시 요청에서도 30분 쿨다운을 보장한다.
        if (!cooldownMarker.tryMark(userId, request.storeId())) {
            throw new CooldownActiveException();
        }

        try {
            CrowdReport saved = crowdReportRepository.save(
                    CrowdReport.create(request.storeId(), userId, request.level()));

            // 혼잡도 리포트 생성 event 발행
            eventPublisher.publishEvent(new CrowdReportCompleted(saved.getId(), userId, request.storeId()));

            return new CrowdReportDto(saved.getId());
        } catch (RuntimeException e) {
            cooldownMarker.cancel(userId, request.storeId());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public CongestionResult getCongestion(Long storeId, double latitude, double longitude) {
        StoreInfo store = storeApi.getById(storeId);
        double distanceMeters = HaversineUtils.distanceMeters(
                latitude,
                longitude,
                store.latitude(),
                store.longitude()
        );

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(COOLDOWN_MINUTES);
        // 최근 30분동안 달린 혼잡도 리포트 조회
        var levels = crowdReportRepository.findLevelsByStoreIdAndCreatedAtAfter(storeId, cutoff);

        if (levels.isEmpty()) {
            return CongestionResult.none(distanceMeters);
        }

        return CongestionResult.of(getMostSelectedLevel(levels), levels.size(), distanceMeters);
    }

    // 선택된 개수 -> level 의 priority 순으로 비교하여 level 추출
    private CongestionLevel getMostSelectedLevel(List<CongestionLevel> levels) {
        Map<CongestionLevel, Long> countByLevel = levels.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Comparator<Map.Entry<CongestionLevel, Long>> comparator = Comparator
                .comparingLong((Map.Entry<CongestionLevel, Long> entry) -> entry.getValue())
                .thenComparingInt(entry -> entry.getKey().getPriority());

        return countByLevel.entrySet()
                .stream()
                .max(comparator)
                .map(Map.Entry::getKey)
                .orElseThrow();
    }
}
