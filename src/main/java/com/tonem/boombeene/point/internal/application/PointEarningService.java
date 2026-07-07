package com.tonem.boombeene.point.internal.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointEarningService {

    private final PessimisticPointBalanceLockStrategy pessimisticPointBalanceLockStrategy;
    private final OptimisticPointBalanceLockStrategy optimisticPointBalanceLockStrategy;
    private final RedisPointBalanceLockStrategy redisPointBalanceLockStrategy;

    // 전략 패턴을 통해 동적 lock 방식 교체
    public void earnForCrowdReport(Long userId, Long reportId, String lockType) {
        PointBalanceLockStrategy strategy = switch (lockType) {
            case "OPTIMISTIC" -> optimisticPointBalanceLockStrategy;
            case "PESSIMISTIC" -> pessimisticPointBalanceLockStrategy;
            case "REDIS" -> redisPointBalanceLockStrategy;
            default -> throw new IllegalArgumentException();
        };

        strategy.earnForCrowdReport(userId, reportId);
    }
}
