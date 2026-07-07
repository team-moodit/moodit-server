package com.team.moodit.domain.user;

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
        UserPrivacyEntity userPrivacy = userPrivacyRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        userPrivacy.applyName(name);

        return userPrivacy.getId();
    }
}
