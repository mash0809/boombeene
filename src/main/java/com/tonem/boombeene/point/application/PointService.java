package com.tonem.boombeene.point.application;

import com.tonem.boombeene.crowdreport.event.CrowdReportCompleted;
import com.tonem.boombeene.point.entity.PointLedger;
import com.tonem.boombeene.point.entity.UserPoint;
import com.tonem.boombeene.point.repository.PointLedgerRepository;
import com.tonem.boombeene.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final int POINT_PER_REPORT = 10;

    private final UserPointRepository userPointRepository;
    private final PointLedgerRepository pointLedgerRepository;

    @ApplicationModuleListener
    public void on(CrowdReportCompleted event) {
        String idempotencyKey = PointLedger.earnKey(event.userId(), event.reportId());
        if (pointLedgerRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        UserPoint userPoint = userPointRepository.findByUserId(event.userId())
                .orElseGet(() -> UserPoint.create(event.userId()));
        userPoint.addBalance(POINT_PER_REPORT);
        userPointRepository.save(userPoint);

        pointLedgerRepository.save(
                PointLedger.earn(event.userId(), event.reportId(), POINT_PER_REPORT, "혼잡도 제보 적립"));
    }
}
