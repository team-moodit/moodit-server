package com.team.moodit.domain.user.privacy;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.storage.db.core.UserPrivacyEntity;
import com.team.moodit.storage.db.core.UserPrivacyRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserPrivacyModifier {
    private final UserPrivacyRepository userPrivacyRepository;

    @Transactional
    public Long modifyName(Long userId, String name) {
        UserPrivacyEntity userPrivacy = userPrivacyRepository.findByUserIdAndStatus(userId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        userPrivacy.applyName(name);

        return userPrivacy.getId();
    }
}
