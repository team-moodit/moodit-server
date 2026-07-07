package com.team.moodit.domain.user.privacy;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.storage.db.core.UserPrivacyEntity;
import com.team.moodit.storage.db.core.UserPrivacyRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPrivacyReader {
    private final UserPrivacyRepository userPrivacyRepository;

    public UserPrivacy getUserPrivacy(Long userId) {
        UserPrivacyEntity entity = userPrivacyRepository.findByUserIdAndStatus(userId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));
        return new UserPrivacy(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getEmail()
        );
    }
}
