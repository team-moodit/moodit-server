package com.team.untitle.clients.kakao;

import com.team.untitle.clients.kakao.model.KakaoClientProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoApiClient {
    private final KakaoApi kakaoApi;

    public KakaoClientProfileResult getProfile(String accessToken) {
        return kakaoApi.getProfile(
                "Bearer " + accessToken,
                "[" + "]"
        ).toResult();
    }
}
