package com.team.moodit.domain.user;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.storage.db.core.UserAuthIdentityEntity;
import com.team.moodit.storage.db.core.UserAuthIdentityRepository;
import com.team.moodit.storage.db.core.UserEntity;
import com.team.moodit.storage.db.core.UserPrivacyRepository;
import com.team.moodit.storage.db.core.UserRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawalProcessor implements UserWithdrawalPostProcessor {
    private final UserRepository userRepository;
    private final UserAuthIdentityRepository userAuthIdentityRepository;

    @Override
    @Transactional
    public void process(Long userId) {
        UserEntity user = userRepository.findByIdAndStatus(userId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));
        UserAuthIdentityEntity authIdentity = userAuthIdentityRepository.findByUserIdAndStatus(user.getId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        authIdentity.delete();
        user.delete();
        log.info("[UserWithdrawalProcessor] 탈퇴 처리 완료 userId: {}", userId);
    }
}
