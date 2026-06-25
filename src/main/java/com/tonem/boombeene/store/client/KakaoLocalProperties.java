package com.tonem.boombeene.store.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.local")
public record KakaoLocalProperties(String baseUrl, String apiKey) {
}
