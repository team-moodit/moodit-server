package com.team.untitle.clients.kakao;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface KakaoAuthApi {
    @PostExchange("/oauth/token")
    KakaoTokenResponse getAccessToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code,
            @RequestParam("client_secret") String clientSecret
    );

    default KakaoTokenResponse getAccessToken(
            String clientId,
            String redirectUri,
            String code,
            String clientSecret
    ) {
        return getAccessToken("authorization_code", clientId, redirectUri, code, clientSecret);
    }
}
