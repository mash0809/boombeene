package com.tonem.boombeene.store.client;

import com.tonem.boombeene.store.exception.KakaoApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoLocalApiClientTest {

    @Test
    void searchByCategoryParsesDocuments() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var client = new KakaoLocalApiClient(builder.build());

        server.expect(requestTo("https://dapi.kakao.com/v2/local/search/category.json?category_group_code=FD6&x=127.027583&y=37.498095&radius=500"))
                .andRespond(withSuccess("""
                        {"documents": [{"id": "12345", "place_name": "테스트 식당", "x": "127.027583", "y": "37.498095"}]}
                        """, MediaType.APPLICATION_JSON));

        var documents = client.searchByCategory(37.498095, 127.027583, 500, "FD6");

        assertThat(documents).hasSize(1);
        assertThat(documents.getFirst().id()).isEqualTo("12345");
        assertThat(documents.getFirst().placeName()).isEqualTo("테스트 식당");
    }

    @Test
    void searchByCategoryWrapsServerErrorAsKakaoApiException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var client = new KakaoLocalApiClient(builder.build());

        server.expect(requestTo("https://dapi.kakao.com/v2/local/search/category.json?category_group_code=FD6&x=127.027583&y=37.498095&radius=500"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.searchByCategory(37.498095, 127.027583, 500, "FD6"))
                .isInstanceOf(KakaoApiException.class);
    }

    @Test
    void searchByCategoryWrapsNullBodyAsKakaoApiException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var client = new KakaoLocalApiClient(builder.build());

        server.expect(requestTo("https://dapi.kakao.com/v2/local/search/category.json?category_group_code=FD6&x=127.027583&y=37.498095&radius=500"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.searchByCategory(37.498095, 127.027583, 500, "FD6"))
                .isInstanceOf(KakaoApiException.class);
    }
}
