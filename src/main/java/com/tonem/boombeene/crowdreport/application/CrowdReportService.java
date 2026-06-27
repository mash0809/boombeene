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

    @Transactional
    public CrowdReportDto report(Long userId, CrowdReportRequest request) {
        StoreInfo store = storeFacade.getById(request.storeId());

        if (!HaversineUtils.isWithinRadius(
                request.latitude(), request.longitude(),
                store.latitude(), store.longitude(),
                request.gpsAccuracy())) {
            throw new LocationTooFarException();
        }

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        if (crowdReportRepository.existsByUserIdAndStoreIdAndCreatedAtAfter(
                userId, request.storeId(), cutoff)) {
            throw new CooldownActiveException();
        }

        CrowdReport saved = crowdReportRepository.save(
                CrowdReport.create(request.storeId(), userId, request.level()));

        eventPublisher.publishEvent(new CrowdReportCompleted(saved.getId(), userId, request.storeId()));

        return new CrowdReportDto(saved.getId());
    }

    @Transactional(readOnly = true)
    public CongestionResult getCongestion(Long storeId) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        var levels = crowdReportRepository.findLevelsByStoreIdAndCreatedAtAfter(storeId, cutoff);

        if (levels.isEmpty()) {
            return CongestionResult.none();
        }

        CongestionLevel winner = levels.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.<CongestionLevel, Long>comparingByValue()
                        .thenComparingInt(entry -> entry.getKey().ordinal()))
                .map(java.util.Map.Entry::getKey)
                .orElseThrow();

        return CongestionResult.of(winner);
    }
}
