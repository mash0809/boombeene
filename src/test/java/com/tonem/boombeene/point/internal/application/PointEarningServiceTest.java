package com.tonem.boombeene.point.internal.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointEarningServiceTest {

    @Mock
    private PointBalanceLockStrategy pointBalanceLockStrategy;

    @InjectMocks
    private PointEarningService pointEarningService;

    @Test
    void earnForCrowdReportDelegatesToConfiguredLockStrategy() {
        pointEarningService.earnForCrowdReport(10L, 100L);

        verify(pointBalanceLockStrategy).earnForCrowdReport(10L, 100L);
    }
}
