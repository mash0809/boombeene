package com.tonem.boombeene.common.lock;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LockAspect {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(com.tonem.boombeene.common.lock.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock lock = method.getAnnotation(DistributedLock.class);
        String key = resolveKey(joinPoint, method, lock.key());

        RLock rLock = redissonClient.getLock(key);
        // 락 획득 실패
        if (!rLock.tryLock(lock.waitTime(), lock.leaseTime(), lock.timeUnit())) {
            throw new LockAcquisitionException("락 획득에 실패했습니다: " + key);
        }

        try {
            return joinPoint.proceed();
        } finally {
            // finally 로 lock 해제 보장
            rLock.unlock();
        }
    }

    // SpEL 표현식 -> 실제 파라미터 값을 기반으로 String key 로 치환 (ex: "'point:lock:' + #userId" -> "point:lock:10")
    private String resolveKey(ProceedingJoinPoint joinPoint, Method method, String keyExpression) {
        var context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(),
                method,
                joinPoint.getArgs(),
                parameterNameDiscoverer
        );

        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
