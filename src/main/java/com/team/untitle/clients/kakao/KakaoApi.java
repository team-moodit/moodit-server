package com.team.untitle.clients.kakao;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface KakaoApi {
    @PostExchange("/v2/user/me")
    KakaoProfileResponse getProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String accessToken,
            @RequestParam("property_keys") String propertyKey
    );
}
