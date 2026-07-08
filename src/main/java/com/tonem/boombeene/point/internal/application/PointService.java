package com.tonem.boombeene.point.internal.application;

import com.tonem.boombeene.common.lock.DistributedLock;
import com.tonem.boombeene.point.internal.entity.PointLedger;
import com.tonem.boombeene.point.internal.entity.UserPoint;
import com.tonem.boombeene.point.internal.repository.PointLedgerRepository;
import com.tonem.boombeene.point.internal.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private static final int POINT_PER_REPORT = 10;
    private static final String EARN_DESCRIPTION = "혼잡도 제보 적립";

    private final UserPointRepository userPointRepository;
    private final PointLedgerRepository pointLedgerRepository;

    public int getBalance(long userId) {
        return userPointRepository.findByUserId(userId)
                .map(UserPoint::getBalance)
                .orElse(0);
    }

    @DistributedLock(key = "'point:lock:' + #userId")
    @Transactional
    public void updateBalance(Long userId, Long reportId) {
        String idempotencyKey = PointLedger.earnKey(userId, reportId);
        if (pointLedgerRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        UserPoint userPoint = userPointRepository.findByUserId(userId)
                .orElseGet(() -> UserPoint.create(userId));
        userPoint.addBalance(POINT_PER_REPORT);
        userPointRepository.save(userPoint);

        pointLedgerRepository.save(PointLedger.earn(userId, reportId, POINT_PER_REPORT, EARN_DESCRIPTION));
    }
}
