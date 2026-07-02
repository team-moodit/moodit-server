package com.team.moodit.api.controller.v1;

import com.team.moodit.api.controller.v1.request.MatchCreateRequest;
import com.team.moodit.api.controller.v1.request.VoteSaveRequest;
import com.team.moodit.api.controller.v1.response.MatchCreateResponse;
import com.team.moodit.api.controller.v1.response.MatchStartResponse;
import com.team.moodit.api.controller.v1.response.MatchTabResponse;
import com.team.moodit.api.controller.v1.response.MatchUpFlowResponse;
import com.team.moodit.api.controller.v1.response.MatchUpWinnerResponse;
import com.team.moodit.api.controller.v1.response.VoteSaveResponse;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.domain.match.MatchTab;
import com.team.moodit.domain.match.MatchUpFinder;
import com.team.moodit.domain.match.MatchUpStart;
import com.team.moodit.domain.match.MatchUpWinnerResultManager;
import com.team.moodit.domain.match.MatchVoteManager;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final MatchVoteManager matchVoteManager;
    private final MatchUpFinder matchUpFinder;
    private final MatchUpWinnerResultManager matchUpWinnerResultManager;

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
        VoteSaveResponse response = matchVoteManager.processVote(matchId, apiUser.getId(), request.toCommand());

        return ApiResponse.success(response);
    }

    @GetMapping("/v1/matches/{matchId}/next-matchup")
    public ApiResponse<MatchUpFlowResponse> getNextMatchUp(
            ApiUser apiUser,
            @PathVariable Long matchId
    ) {
        MatchUpFlowResponse response = matchUpFinder.findNextMatchUp(matchId, apiUser.getId());
        return ApiResponse.success(response);
    }

    @GetMapping("/v1/matches/{matchId}/completed")
    public ApiResponse<MatchUpWinnerResponse> getMatchUpWinner(
            ApiUser apiUser,
            @PathVariable Long matchId
    ) {
        // 1. 우리가 고쳐놓은 매니저를 호출해서 읽기/쓰기가 통합된 MatchResult 도메인을 획득합니다.
        MatchResult winnerDomain = matchUpWinnerResultManager.getOrCreateMatchUpWinnerResult(matchId, apiUser.getId());

        // 2. 방금 DTO에 새로 추가한 생성자(MatchResult를 인자로 받는) 덕분에 바로 꽂아 넣을 수 있습니다!
        return ApiResponse.success(new MatchUpWinnerResponse(winnerDomain));

    }

    @DeleteMapping("/v1/matches/{matchId}")
    public ApiResponse<String> deleteMatchUp(
            ApiUser apiUser,
            @PathVariable Long matchId
    ) {
        matchService.deleteMatch(apiUser.getId(), matchId);
        return ApiResponse.success("매치 " + matchId + "번과 관련된 모든 데이터가 클린하게 삭제되었습니다.");
    }
    @GetMapping("/v1/matches/moodtab")
    public ApiResponse<MatchTabResponse> getTabMatchup(
            ApiUser apiUser,
            @RequestParam(defaultValue = "0") int inProgressPage,
            @RequestParam(defaultValue = "5") int inProgressSize,
            @RequestParam(defaultValue = "0") int completedPage,
            @RequestParam(defaultValue = "10") int completedSize
    ) {
        MatchTab matchTab = matchService.getMatchTab(
                apiUser.getId(),
                inProgressPage,
                inProgressSize,
                completedPage,
                completedSize
        );

        return ApiResponse.success(MatchTabResponse.of(matchTab));
    }

}


