package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.user.UserProfile;

public record UserProfileResponse(
        Long id,
        String nickname
) {
    public static UserProfileResponse of(
            UserProfile userProfile
    ) {
        return new UserProfileResponse(
                userProfile.getId(),
                "이름카카오심사중아직없음~"
        );
    }
}
