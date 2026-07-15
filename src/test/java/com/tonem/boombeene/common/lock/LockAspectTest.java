package com.tonem.boombeene.common.lock;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("SpEL로 해석한 키로 락을 걸고 원본 메소드의 반환값을 그대로 반환한다")
    void locksWithResolvedSpelKeyAndReturnsProceedResult() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(lockTargetMethod());
        when(joinPoint.getTarget()).thenReturn(new TestLockTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, 100L});
        when(redissonClient.getLock("point:lock:10")).thenReturn(rLock);
        when(rLock.tryLock(3L, 5L, TimeUnit.SECONDS)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = lockAspect.lock(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(redissonClient).getLock("point:lock:10");
        verify(joinPoint).proceed();
        verify(rLock).unlock();
    }

    @Test
    @DisplayName("락 획득에 실패하면 예외를 던진다")
    void throwsWhenLockIsNotAcquired() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(lockTargetMethod());
        when(joinPoint.getTarget()).thenReturn(new TestLockTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, 100L});
        when(redissonClient.getLock("point:lock:10")).thenReturn(rLock);
        when(rLock.tryLock(3L, 5L, TimeUnit.SECONDS)).thenReturn(false);

        assertThatThrownBy(() -> lockAspect.lock(joinPoint))
                .isInstanceOf(LockAcquisitionException.class)
                .hasMessage("락 획득에 실패했습니다: point:lock:10");

        verify(joinPoint, never()).proceed();
        verify(rLock, never()).unlock();
    }

    @Test
    @DisplayName("원본 메소드 실행 중 예외가 발생해도 락을 해제한다")
    void unlocksWhenProceedThrowsException() throws Throwable {
        RuntimeException failure = new RuntimeException("failure");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(lockTargetMethod());
        when(joinPoint.getTarget()).thenReturn(new TestLockTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, 100L});
        when(redissonClient.getLock("point:lock:10")).thenReturn(rLock);
        when(rLock.tryLock(3L, 5L, TimeUnit.SECONDS)).thenReturn(true);
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> lockAspect.lock(joinPoint))
                .isSameAs(failure);

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("락 대기 중 인터럽트가 발생하면 인터럽트 상태를 복원하고 예외를 던진다")
    void restoresInterruptStatusWhenTryLockIsInterrupted() throws Throwable {
        InterruptedException interruptedException = new InterruptedException("interrupted");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(lockTargetMethod());
        when(joinPoint.getTarget()).thenReturn(new TestLockTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, 100L});
        when(redissonClient.getLock("point:lock:10")).thenReturn(rLock);
        when(rLock.tryLock(3L, 5L, TimeUnit.SECONDS)).thenThrow(interruptedException);

        assertThatThrownBy(() -> lockAspect.lock(joinPoint))
                .isInstanceOf(LockAcquisitionException.class)
                .hasMessage("락 획득 대기 중 인터럽트가 발생했습니다: point:lock:10")
                .hasCause(interruptedException);

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        verify(joinPoint, never()).proceed();
        verify(rLock, never()).unlock();
        Thread.interrupted();
    }

    private Method lockTargetMethod() throws NoSuchMethodException {
        return TestLockTarget.class.getDeclaredMethod("updateBalance", Long.class, Long.class);
    }

    private static class TestLockTarget {

        @DistributedLock(key = "'point:lock:' + #userId", waitTime = 3, leaseTime = 5, timeUnit = TimeUnit.SECONDS)
        void updateBalance(Long userId, Long reportId) {
        }
    }
}
