package com.team.moodit.api.controller.v1.response;

import com.team.moodit.domain.user.UserPrivacy;

public record UserPrivacyInfoResponse(
        String name,
        String email
) {
    public static UserPrivacyInfoResponse of(
            UserPrivacy userPrivacy
    ) {
        return new UserPrivacyInfoResponse(
                userPrivacy.getName() != null ? userPrivacy.getName() : null,
                userPrivacy.getEmail()
        );
    }
}
