package com.tonem.boombeene.point.internal.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PessimisticPointBalanceLockStrategy implements PointBalanceLockStrategy {

    private final PointBalanceService pointBalanceService;

    @Override
    public void earnForCrowdReport(Long userId, Long reportId) {
        pointBalanceService.earnForCrowdReport(userId, reportId, true);
    }
}
