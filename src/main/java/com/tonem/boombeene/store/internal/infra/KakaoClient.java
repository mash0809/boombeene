package com.tonem.boombeene.store.internal.infra;

import com.tonem.boombeene.store.internal.exception.KakaoApiException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(KakaoLocalProperties.class)
public class KakaoClient {

    private final static String AUTH_HEADER_PREFIX = "KakaoAK ";

    // RestClient 를 통한 kakao api client 구현
    @Bean
    KakaoLocalApiClient kakaoLocalApiClient(KakaoLocalProperties properties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        var errorHandler = new DefaultResponseErrorHandler();
        var restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, AUTH_HEADER_PREFIX + properties.apiKey())
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
                    try {
                        errorHandler.handleError(req.getURI(), req.getMethod(), res);
                    } catch (RestClientException e) {
                        throw new KakaoApiException(e);
                    }
                })
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(KakaoLocalApiClient.class);
    }
}
