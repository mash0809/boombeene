package com.tonem.boombeene.common.lock;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LockAspectTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private LockAspect lockAspect;

    @BeforeEach
    void setUp() {
        lockAspect = new LockAspect(redissonClient);
    }

    @Test
    void locksWithResolvedSpelKeyAndReturnsProceedResult() throws Throwable {
        DistributedLock distributedLock = distributedLock();
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(lockTargetMethod());
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, 100L});
        when(redissonClient.getLock("point:lock:10")).thenReturn(rLock);
        when(rLock.tryLock(3L, 5L, TimeUnit.SECONDS)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = lockAspect.lock(joinPoint, distributedLock);

        assertThat(result).isEqualTo("ok");
        verify(redissonClient).getLock("point:lock:10");
        verify(joinPoint).proceed();
        verify(rLock).unlock();
    }

    @Test
    void throwsWhenLockIsNotAcquired() throws Throwable {
        DistributedLock distributedLock = distributedLock();
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(lockTargetMethod());
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, 100L});
        when(redissonClient.getLock("point:lock:10")).thenReturn(rLock);
        when(rLock.tryLock(3L, 5L, TimeUnit.SECONDS)).thenReturn(false);

        assertThatThrownBy(() -> lockAspect.lock(joinPoint, distributedLock))
                .isInstanceOf(LockAcquisitionException.class)
                .hasMessage("락 획득에 실패했습니다: point:lock:10");

        verify(joinPoint, never()).proceed();
        verify(rLock, never()).unlock();
    }

    @Test
    void unlocksWhenProceedThrowsException() throws Throwable {
        DistributedLock distributedLock = distributedLock();
        RuntimeException failure = new RuntimeException("failure");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(lockTargetMethod());
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, 100L});
        when(redissonClient.getLock("point:lock:10")).thenReturn(rLock);
        when(rLock.tryLock(3L, 5L, TimeUnit.SECONDS)).thenReturn(true);
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> lockAspect.lock(joinPoint, distributedLock))
                .isSameAs(failure);

        verify(rLock).unlock();
    }

    private DistributedLock distributedLock() throws NoSuchMethodException {
        return lockTargetMethod().getAnnotation(DistributedLock.class);
    }

    private Method lockTargetMethod() throws NoSuchMethodException {
        return TestLockTarget.class.getDeclaredMethod("updateBalance", Long.class, Long.class);
    }

    private static class TestLockTarget {

        @DistributedLock(key = "'point:lock:' + #userId")
        void updateBalance(Long userId, Long reportId) {
        }
    }
}
