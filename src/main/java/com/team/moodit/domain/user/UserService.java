package com.team.moodit.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserPrivacyReader userPrivacyReader;

    public UserPrivacy getUserPrivacy(Long userId) {
        return userPrivacyReader.getUserPrivacy(userId);
    }
}
