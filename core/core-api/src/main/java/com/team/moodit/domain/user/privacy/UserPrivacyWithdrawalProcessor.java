package com.team.moodit.domain.user.privacy;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.user.UserWithdrawalPostProcessor;
import com.team.moodit.storage.db.core.BaseEntity;
import com.team.moodit.storage.db.core.UserPrivacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPrivacyWithdrawalProcessor implements UserWithdrawalPostProcessor {
    private final UserPrivacyRepository userPrivacyRepository;

    @Override
    @Transactional
    public void process(Long userId) {
        userPrivacyRepository.findByUserIdAndStatus(userId, EntityStatus.ACTIVE)
                .ifPresent(BaseEntity::delete);
        log.info("[UserPrivacyWithdrawalProcessor] 탈퇴 처리 완료 userId: {}", userId);
    }
}
