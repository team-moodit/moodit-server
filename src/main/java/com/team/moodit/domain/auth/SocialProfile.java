package com.team.moodit.domain.auth;

import com.team.moodit.domain.enums.SocialProviderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialProfile {
    private SocialProviderType provider;
    private String providerUserId;
    private String providerUserEmail;
}
