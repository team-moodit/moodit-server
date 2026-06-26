package com.team.moodit.api.controller.v1.request;

import com.team.moodit.domain.match.VoteCommand;

public record VoteSaveRequest(
        Long photoId,   // 사용자가 선택한 사진의 식별자 (필수)
        Long reasonId   // 선택 이유(상세선호)의 식별자 (필수)
) {


    public VoteCommand toCommand() {
        // 롬복 Getter 이름 규칙(getPhotoId, getReasonId)이나 필드명에 맞게 매핑해줍니다.
        return new VoteCommand(this.photoId, this.reasonId);
    }
}
