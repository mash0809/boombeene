package com.tonem.boombeene.crowdreport.internal.application;

import com.tonem.boombeene.crowdreport.internal.dto.CongestionResult;
import com.tonem.boombeene.crowdreport.internal.dto.CongestionSample;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportDto;
import com.tonem.boombeene.crowdreport.internal.dto.CrowdReportRequest;
import com.tonem.boombeene.crowdreport.internal.dto.HourlyCongestion;
import com.tonem.boombeene.crowdreport.internal.dto.WeeklyCongestionResult;
import com.tonem.boombeene.crowdreport.internal.entity.CongestionLevel;
import com.tonem.boombeene.crowdreport.internal.entity.CrowdReport;
import com.tonem.boombeene.crowdreport.CrowdReportCompleted;
import com.tonem.boombeene.crowdreport.internal.exception.CooldownActiveException;
import com.tonem.boombeene.crowdreport.internal.exception.LocationTooFarException;
import com.tonem.boombeene.crowdreport.internal.repository.CrowdReportRepository;
import com.tonem.boombeene.crowdreport.internal.util.HaversineUtils;
import com.tonem.boombeene.store.StoreApi;
import com.tonem.boombeene.store.StoreInfo;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrowdReportService {

    private final StoreApi storeApi;
    private final CrowdReportRepository crowdReportRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CrowdReportCooldownMarker cooldownMarker;

    private static final int CONGESTION_WINDOW_MINUTES = 30;
    private static final int RECENT_COMMENTS_LIMIT = 5;
    private static final int WEEKLY_WINDOW_DAYS = 28;

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

        // 아직 해당 스토어에 혼잡도 리포트 할 수 없는 경우 (SET NX PX 로 write 시도 실패)
        if (!cooldownMarker.tryMark(userId, request.storeId())) {
            throw new CooldownActiveException();
        }

        try {
            CrowdReport saved = crowdReportRepository.save(
                    CrowdReport.create(request.storeId(), userId, request.level(), request.comment()));

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

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(CONGESTION_WINDOW_MINUTES);
        // 최근 30분동안 달린 혼잡도 리포트 조회
        var levels = crowdReportRepository.findLevelsByStoreIdAndCreatedAtAfter(storeId, cutoff);

        if (levels.isEmpty()) {
            return CongestionResult.none(distanceMeters);
        }

        var comments = crowdReportRepository.findRecentComments(
                storeId,
                cutoff,
                PageRequest.of(0, RECENT_COMMENTS_LIMIT)
        );

        return CongestionResult.of(getMostSelectedLevel(levels), levels.size(), distanceMeters, comments);
    }

    @Transactional(readOnly = true)
    public List<WeeklyCongestionResult> getWeeklyCongestion(Long storeId) {
        storeApi.validateExists(storeId);

        LocalDateTime cutoff = LocalDate.now().minusDays(WEEKLY_WINDOW_DAYS).atStartOfDay();
        var records = crowdReportRepository.findByStoreIdAndCreatedAtAfter(storeId, cutoff);

        Map<DayOfWeek, List<CongestionLevel>> dailyLevels = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getCreatedAt().getDayOfWeek(),
                        Collectors.mapping(CongestionSample::getLevel, Collectors.toList())
                ));

        Map<DayOfWeek, Map<Integer, List<CongestionLevel>>> hourlyLevels = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getCreatedAt().getDayOfWeek(),
                        Collectors.groupingBy(
                                record -> record.getCreatedAt().getHour(),
                                Collectors.mapping(CongestionSample::getLevel, Collectors.toList())
                        )
                ));

        return Arrays.stream(DayOfWeek.values())
                .map(day -> {
                    List<CongestionLevel> levels = dailyLevels.getOrDefault(day, List.of());
                    return new WeeklyCongestionResult(
                            day,
                            averageLevel(levels),
                            levels.size(),
                            buildHourly(hourlyLevels.getOrDefault(day, Map.of()))
                    );
                })
                .toList();
    }

    private List<HourlyCongestion> buildHourly(Map<Integer, List<CongestionLevel>> hourGroups) {
        return IntStream.range(0, 24)
                .mapToObj(hour -> {
                    List<CongestionLevel> levels = hourGroups.getOrDefault(hour, List.of());
                    return new HourlyCongestion(hour, averageLevel(levels), levels.size());
                })
                .toList();
    }

    // 레벨 priority 평균을 반올림해 레벨로 환산한다. 리포트가 없으면 데이터 없음(null)으로 반환한다.
    private CongestionLevel averageLevel(List<CongestionLevel> levels) {
        if (levels.isEmpty()) {
            return null;
        }
        double average = levels.stream()
                .mapToInt(CongestionLevel::getPriority)
                .average()
                .orElseThrow();
        return CongestionLevel.fromPriority((int) Math.round(average));
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
