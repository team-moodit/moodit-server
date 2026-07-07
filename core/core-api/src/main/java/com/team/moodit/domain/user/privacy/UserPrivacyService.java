package com.team.moodit.domain.user.privacy;

import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPrivacyService {
    private final UserPrivacyReader userPrivacyReader;
    private final UserPrivacyModifier userPrivacyModifier;

    public UserPrivacy getUserPrivacy(Long userId) {
        return userPrivacyReader.getUserPrivacy(userId);
    }

    public Long updatePrivacyName(ApiUser apiUser, String name) {
        return userPrivacyModifier.modifyName(apiUser.getId(), name);
    }
}
