package com.tonem.boombeene.point.internal.application;

import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptimisticPointBalanceLockStrategy implements PointBalanceLockStrategy {

    private static final int MAX_ATTEMPTS = 5;
    private static final long INITIAL_BACKOFF_MILLIS = 100L;

    private final PointBalanceService pointBalanceService;

    @Override
    public void earnForCrowdReport(Long userId, Long reportId) {
        long backoffMillis = INITIAL_BACKOFF_MILLIS;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                pointBalanceService.earnForCrowdReport(userId, reportId, false);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw e;
                }
                sleep(backoffMillis);
                backoffMillis *= 2;
            }
        }
    }

    private void sleep(long backoffMillis) {
        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("포인트 낙관적 락 재시도 대기 중 인터럽트되었습니다.", e);
        }
    }
}
