package com.team.untitle.domain.auth;

import com.team.untitle.domain.enums.SocialProviderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SocialProfile {
    private SocialProviderType provider;
    private String providerUserId;
}
