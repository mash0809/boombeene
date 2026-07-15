package com.tonem.boombeene.crowdreport.internal.application;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrowdReportCooldownMarker {

    // report 작성 후 같은 store 에 대해서 30분간 작성 제한
    private static final Duration COOLDOWN_TTL = Duration.ofMinutes(30);

    private final RedissonClient redissonClient;

    public boolean tryMark(Long userId, Long storeId) {
        return getBucket(userId, storeId).setIfAbsent("1", COOLDOWN_TTL);
    }

    public void cancel(Long userId, Long storeId) {
        getBucket(userId, storeId).deleteAsync();
    }

    private RBucket<String> getBucket(Long userId, Long storeId) {
        return redissonClient.getBucket(key(userId, storeId));
    }

    private String key(Long userId, Long storeId) {
        return "crowdreport:cooldown:" + userId + ":" + storeId;
    }
}
