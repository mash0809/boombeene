package com.tonem.boombeene.store.internal.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "kakao.local")
public record KakaoLocalProperties(String baseUrl, String apiKey, Duration connectTimeout, Duration readTimeout) {
}
