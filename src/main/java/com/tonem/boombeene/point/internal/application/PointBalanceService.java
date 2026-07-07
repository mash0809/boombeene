package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.point.internal.entity.PointLedger;
import com.tonem.boombeene.point.internal.entity.UserPoint;
import com.tonem.boombeene.point.internal.repository.PointLedgerRepository;
import com.tonem.boombeene.point.internal.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PointBalanceService {

    private static final int POINT_PER_REPORT = 10;

    private final UserPointRepository userPointRepository;
    private final PointLedgerRepository pointLedgerRepository;

    @Transactional
    public void earnForCrowdReport(
            Long userId,
            Long reportId,
            boolean forUpdateLock
    ) {
        String idempotencyKey = PointLedger.earnKey(userId, reportId);
        if (pointLedgerRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        UserPoint userPoint;
        if (forUpdateLock) {
            userPoint = userPointRepository.findWithLockByUserId(userId).orElse(null);
        } else {
            userPoint = userPointRepository.findByUserId(userId).orElse(null);
        }

        if (userPoint == null) {
            userPoint = UserPoint.create(userId);
        }

        userPoint.addBalance(POINT_PER_REPORT);
        userPointRepository.save(userPoint);

        pointLedgerRepository.save(
                PointLedger.earn(userId, reportId, POINT_PER_REPORT, "혼잡도 제보 적립"));
    }
}
