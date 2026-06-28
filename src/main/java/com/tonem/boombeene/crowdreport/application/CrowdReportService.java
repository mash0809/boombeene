package com.tonem.boombeene.crowdreport.application;

import com.tonem.boombeene.crowdreport.dto.CongestionResult;
import com.tonem.boombeene.crowdreport.dto.CrowdReportDto;
import com.tonem.boombeene.crowdreport.dto.CrowdReportRequest;
import com.tonem.boombeene.crowdreport.entity.CongestionLevel;
import com.tonem.boombeene.crowdreport.entity.CrowdReport;
import com.tonem.boombeene.crowdreport.event.CrowdReportCompleted;
import com.tonem.boombeene.crowdreport.exception.CooldownActiveException;
import com.tonem.boombeene.crowdreport.exception.LocationTooFarException;
import com.tonem.boombeene.crowdreport.repository.CrowdReportRepository;
import com.tonem.boombeene.crowdreport.util.HaversineUtils;
import com.tonem.boombeene.store.api.StoreFacade;
import com.tonem.boombeene.store.api.StoreInfo;
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

    private final StoreFacade storeFacade;
    private final CrowdReportRepository crowdReportRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final int COOLDOWN_MINUTES = 30;

    @Transactional
    public CrowdReportDto report(Long userId, CrowdReportRequest request) {
        StoreInfo store = storeFacade.getById(request.storeId());
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

        // 30분 이내에 이미 같은 가게에 혼잡도 리포트한 경우
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(COOLDOWN_MINUTES);
        if (crowdReportRepository.existsByUserIdAndStoreIdAndCreatedAtAfter(userId, request.storeId(), cutoff)) {
            throw new CooldownActiveException();
        }

        CrowdReport saved = crowdReportRepository.save(
                CrowdReport.create(request.storeId(), userId, request.level()));

        // 혼잡도 리포트 생성 event 발행
        eventPublisher.publishEvent(new CrowdReportCompleted(saved.getId(), userId, request.storeId()));

        return new CrowdReportDto(saved.getId());
    }

    @Transactional(readOnly = true)
    public CongestionResult getCongestion(Long storeId, double latitude, double longitude) {
        StoreInfo store = storeFacade.getById(storeId);
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
