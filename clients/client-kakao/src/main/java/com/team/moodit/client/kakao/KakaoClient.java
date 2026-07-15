package com.team.moodit.client.kakao;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoClient {
    @Value("${kakao-api.admin-key}")
    private String adminKey;

    private final KakaoApi kakaoApi;

    public KakaoClientProfileResult getProfile(String accessToken) {
        return kakaoApi.getProfile(
                "Bearer " + accessToken,
                "[\"kakao_account.email\"]"
        ).toResult();
    }

    public void unlinkAccount(String kakaoUserId) {
        kakaoApi.unlinkAccount(
                "KakaoAK " + adminKey,
                "user_id",
                Long.parseLong(kakaoUserId)
        );
    }
}
