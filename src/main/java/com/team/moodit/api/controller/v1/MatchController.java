package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.request.MatchCreateRequest;
import com.team.moodit.api.controller.v1.response.MatchCreateResponse;
import com.team.moodit.domain.match.MatchCreateResult;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
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
            ){
        MatchCreateResult matchCreateResult = matchService.createMatch(
                apiUser.getId(),
                request.title(),
                request.images()
        );
        return ApiResponse.success(new MatchCreateResponse(matchCreateResult.getMatchId()));

    }
}
