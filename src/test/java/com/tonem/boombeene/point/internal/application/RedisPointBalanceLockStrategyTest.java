package com.tonem.boombeene.point.internal.application;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisPointBalanceLockStrategyTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @Mock
    private PointBalanceService pointBalanceService;

    @InjectMocks
    private RedisPointBalanceLockStrategy redisPointBalanceLockStrategy;

    @Test
    void earnForCrowdReportUsesRedissonLock() throws InterruptedException {
        when(redissonClient.getLock("point:lock:10")).thenReturn(lock);
        when(lock.tryLock(3, 5, TimeUnit.SECONDS)).thenReturn(true);

        redisPointBalanceLockStrategy.earnForCrowdReport(10L, 100L);

        verify(pointBalanceService).earnForCrowdReport(10L, 100L, false);
        verify(lock).unlock();
    }

    @Test
    void earnForCrowdReportDoesNotUpdatePointWhenLockIsUnavailable() throws InterruptedException {
        when(redissonClient.getLock("point:lock:10")).thenReturn(lock);
        when(lock.tryLock(3, 5, TimeUnit.SECONDS)).thenReturn(false);

        assertThatThrownBy(() -> redisPointBalanceLockStrategy.earnForCrowdReport(10L, 100L))
                .isInstanceOf(IllegalStateException.class);

        verify(pointBalanceService, never()).earnForCrowdReport(10L, 100L, false);
        verify(lock, never()).unlock();
    }
}
