package com.tonem.boombeene.crowdreport.internal.application;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrowdReportCooldownMarkerTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> bucket;

    private CrowdReportCooldownMarker cooldownMarker;

    @BeforeEach
    void setUp() {
        cooldownMarker = new CrowdReportCooldownMarker(redissonClient);
    }

    @Test
    void tryMarkReturnsTrueWhenCooldownMarkerIsCreated() {
        when(redissonClient.<String>getBucket("crowdreport:cooldown:10:1")).thenReturn(bucket);
        when(bucket.setIfAbsent("1", Duration.ofMinutes(30))).thenReturn(true);

        boolean marked = cooldownMarker.tryMark(10L, 1L);

        assertThat(marked).isTrue();
        verify(redissonClient).getBucket("crowdreport:cooldown:10:1");
        verify(bucket).setIfAbsent("1", Duration.ofMinutes(30));
    }

    @Test
    void tryMarkReturnsFalseWhenCooldownMarkerAlreadyExists() {
        when(redissonClient.<String>getBucket("crowdreport:cooldown:10:1")).thenReturn(bucket);
        when(bucket.setIfAbsent("1", Duration.ofMinutes(30))).thenReturn(false);

        boolean marked = cooldownMarker.tryMark(10L, 1L);

        assertThat(marked).isFalse();
    }

    @Test
    void cancelDeletesCooldownMarkerWithSameKey() {
        when(redissonClient.<String>getBucket("crowdreport:cooldown:10:1")).thenReturn(bucket);

        cooldownMarker.cancel(10L, 1L);

        verify(redissonClient).getBucket("crowdreport:cooldown:10:1");
        verify(bucket).deleteAsync();
    }
}
