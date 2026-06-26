package com.team.moodit.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserProfileReader userProfileReader;

    public UserProfile getProfile(Long userId) {
        return userProfileReader.getUserProfile(userId);
    }
}
