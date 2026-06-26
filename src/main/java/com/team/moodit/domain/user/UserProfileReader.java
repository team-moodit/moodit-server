package com.team.moodit.domain.user;

import com.team.moodit.storage.db.core.UserProfileEntity;
import com.team.moodit.storage.db.core.UserProfileRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileReader {
    private final UserProfileRepository userProfileRepository;

    public UserProfile getUserProfile(Long userId) {
        UserProfileEntity entity = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        return new UserProfile(
                entity.getId(),
                entity.getUserId(),
                entity.getEmail(),
                entity.getNickname()
        );
    }
}
