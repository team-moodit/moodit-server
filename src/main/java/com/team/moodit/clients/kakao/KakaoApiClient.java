package com.team.moodit.clients.kakao;

import com.team.moodit.clients.kakao.model.KakaoClientProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoApiClient {
    private final KakaoApi kakaoApi;

    public KakaoClientProfileResult getProfile(String accessToken) {
        return kakaoApi.getProfile(
                "Bearer " + accessToken,
                "[\"kakao_account.email\"]"
        ).toResult();
    }
}
