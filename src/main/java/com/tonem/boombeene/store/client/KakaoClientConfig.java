package com.tonem.boombeene.store.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoLocalProperties.class)
public class KakaoClientConfig {

    private final static String AUTH_HEADER_PREFIX = "KakaoAK ";

    @Bean
    RestClient kakaoRestClient(KakaoLocalProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + properties.apiKey())
                .build();
    }
}
