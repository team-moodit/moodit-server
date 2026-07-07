package com.team.moodit.api.controller.v1.request;

import com.team.moodit.domain.match.VoteCommand;

public record VoteSaveRequest(
        Long matchUpId, // 1. 클라이언트가 투표할 대상 경기 ID를 보낼 필드 추가
        Long photoId,   // 사용자가 선택한 사진의 식별자
        Long reasonId   // 선택 이유의 식별자
) {
    public VoteCommand toCommand() {
        // 2. Command 생성 시 matchUpId를 포함하여 전달
        return new VoteCommand(this.matchUpId, this.photoId, this.reasonId);
    }
}