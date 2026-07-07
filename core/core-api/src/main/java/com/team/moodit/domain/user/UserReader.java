package com.team.moodit.domain.user;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.storage.db.core.UserEntity;
import com.team.moodit.storage.db.core.UserRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReader {
    private final UserRepository userRepository;

    public User getUser(Long userId) {
        UserEntity entity = userRepository.findByIdAndStatus(userId, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        return new User(
                entity.getId(),
                entity.getRole()
        );
    }
}
