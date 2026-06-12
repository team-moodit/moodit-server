package com.team.moodit.clients.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class KakaoConfig {
    @Value("${kakao-api.base-url}")
    private String kakaoApiBaseUrl;

    @Bean
    public KakaoApi kakaoApi() {
        RestClient client = RestClient.builder()
                .baseUrl(kakaoApiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder().exchangeAdapter(adapter).build();
        return factory.createClient(KakaoApi.class);
    }
}
