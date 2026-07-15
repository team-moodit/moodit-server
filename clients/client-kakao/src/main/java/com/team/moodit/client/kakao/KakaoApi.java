package com.team.moodit.client.kakao;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
interface KakaoApi {
    @PostExchange("/v2/user/me")
    KakaoProfileResponse getProfile(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam("property_keys") String propertyKey
    );

    @PostExchange("/v1/user/unlink")
    void unlinkAccount(
            @RequestHeader("Authorization") String adminKey,
            @RequestParam("target_id_type") String targetIdType,
            @RequestParam("target_id") Long targetId
    );
}
