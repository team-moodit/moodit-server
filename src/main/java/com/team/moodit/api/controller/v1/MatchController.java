package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.request.MatchCreateRequest;
import com.team.moodit.api.controller.v1.request.VoteSaveRequest;
import com.team.moodit.api.controller.v1.response.MatchCreateResponse;
import com.team.moodit.api.controller.v1.response.MatchStartResponse;
import com.team.moodit.api.controller.v1.response.MatchUpFlowResponse;
import com.team.moodit.api.controller.v1.response.VoteSaveResponse;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.domain.match.MatchUpFinder;
import com.team.moodit.domain.match.MatchUpStart;
import com.team.moodit.domain.match.MatchVoteManager;
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
    private final MatchVoteManager matchVoteManager;
    private final MatchUpFinder matchUpFinder;

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
            @PathVariable Long matchId
    ) {
        MatchUpStart domainMatch = matchService.getMatchup(matchId);

        return ApiResponse.success(MatchStartResponse.of(domainMatch));
    }

    @PostMapping("/v1/matches/{matchId}/votes")
    public ApiResponse<VoteSaveResponse> saveVote(
            ApiUser apiUser, // 필요 시 사용자 검증 및 히스토리 추적용으로 바인딩 허용
            @PathVariable Long matchId,
            @RequestBody VoteSaveRequest request
    ) {
        // 웹 DTO를 순수한 도메인 Command 메시지로 변환하여 오염 없는 진입을 보장합니다.
        VoteSaveResponse response = matchVoteManager.processVote(matchId, request.toCommand());

        return ApiResponse.success(response);
    }

    @GetMapping("/v1/matches/{matchId}/next-matchup")
    public ApiResponse<MatchUpFlowResponse> getNextMatchUp(
                                                            ApiUser apiUser,
                                                            @PathVariable Long matchId
    ) {
        MatchUpFlowResponse response = matchUpFinder.findNextMatchUp(matchId);
        return ApiResponse.success(response);
    }




}
