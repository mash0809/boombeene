package com.tonem.boombeene.crowdreport.internal.application;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrowdReportCooldownMarker {

    private static final Duration COOLDOWN_TTL = Duration.ofMinutes(30);

    private final RedissonClient redissonClient;

    public boolean tryMark(Long userId, Long storeId) {
        RBucket<String> bucket = redissonClient.getBucket(key(userId, storeId));
        return bucket.setIfAbsent("1", COOLDOWN_TTL);
    }

    public void cancel(Long userId, Long storeId) {
        redissonClient.getBucket(key(userId, storeId)).deleteAsync();
    }

    private String key(Long userId, Long storeId) {
        return "crowdreport:cooldown:" + userId + ":" + storeId;
    }
}
