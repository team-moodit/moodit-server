package com.team.moodit.client.kakao;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoClient {
    private final KakaoApi kakaoApi;

    public KakaoClientProfileResult getProfile(String accessToken) {
        return kakaoApi.getProfile(
                "Bearer " + accessToken,
                "[\"kakao_account.email\"]"
        ).toResult();
    }
}
