package com.team.moodit.domain.user;

import com.team.moodit.domain.auth.SocialProfile;
import com.team.moodit.domain.enums.UserRole;
import com.team.moodit.storage.db.core.UserAuthIdentityEntity;
import com.team.moodit.storage.db.core.UserAuthIdentityRepository;
import com.team.moodit.storage.db.core.UserEntity;
import com.team.moodit.storage.db.core.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserManager {
    private final UserRepository userRepository;
    private final UserAuthIdentityRepository userAuthIdentityRepository;

    @Transactional
    public Long createSocialUser(SocialProfile profile) {
        UserEntity savedUser = userRepository.save(
                new UserEntity(
                        UserRole.USER,
                        LocalDateTime.now()
                )
        );

        userAuthIdentityRepository.save(
                new UserAuthIdentityEntity(
                        savedUser.getId(),
                        profile.getProvider(),
                        profile.getProviderUserId()
                )
        );

        return savedUser.getId();
    }
}
