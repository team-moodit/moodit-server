package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.request.MatchCreateRequest;
import com.team.moodit.api.controller.v1.response.MatchCreateResponse;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;

    @PostMapping("/v1/matches")
    public ApiResponse<MatchCreateResponse> createMatch(
            ApiUser apiUser,
            @RequestBody MatchCreateRequest request
    ) {
        Long successId = matchService.createMatch(
                apiUser,
                request.toNewMatch(),
                request.images()
        );
        return ApiResponse.success(new MatchCreateResponse(successId));
    }

    @GetMapping("/v1/matches/{matchId}/start")
    public ApiResponse<MatchStartResponse> startMatch(
            ApiUser apiUser,
            @PathVariable Long matchId // 👈 요청은 이게 전부입니다!
    ) {
        // 비즈니스 로직 호출
        Match domainMatch = matchManager.start(matchId);

        // Response 반환
        return ApiResponse.success(MatchStartResponse.of(domainMatch));
    }
}
