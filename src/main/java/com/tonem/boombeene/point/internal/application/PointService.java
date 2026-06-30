package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.crowdreport.internal.event.CrowdReportCompleted;
import com.tonem.boombeene.point.internal.entity.PointLedger;
import com.tonem.boombeene.point.internal.entity.UserPoint;
import com.tonem.boombeene.point.internal.repository.PointLedgerRepository;
import com.tonem.boombeene.point.internal.repository.UserPointRepository;
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
        // 동시성 이슈 발생 가능, 개선 필요
        userPoint.addBalance(POINT_PER_REPORT);
        userPointRepository.save(userPoint);

        pointLedgerRepository.save(
                PointLedger.earn(event.userId(), event.reportId(), POINT_PER_REPORT, "혼잡도 제보 적립"));
    }
}
