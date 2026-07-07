package com.tonem.boombeene.point.internal.application;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPointBalanceLockStrategy implements PointBalanceLockStrategy {

    private static final long WAIT_TIME_SECONDS = 3;
    private static final long LEASE_TIME_SECONDS = 5;

    private final RedissonClient redissonClient;

    private final PointBalanceService pointBalanceService;

    @Override
    public void earnForCrowdReport(Long userId, Long reportId) {
        RLock lock = redissonClient.getLock("point:lock:" + userId);
        boolean acquired = tryLock(lock);
        if (!acquired) {
            throw new IllegalStateException("포인트 적립 락 획득에 실패했습니다.");
        }

        try {
            pointBalanceService.earnForCrowdReport(userId, reportId, false);
        } finally {
            lock.unlock();
        }
    }

    private boolean tryLock(RLock lock) {
        try {
            return lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("포인트 적립 락 대기 중 인터럽트되었습니다.", e);
        }
    }
}
