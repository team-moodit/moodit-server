package com.team.untitle.domain;

import com.team.untitle.storage.db.core.UserEntity;
import com.team.untitle.storage.db.core.UserRepository;
import com.team.untitle.support.error.ApiException;
import com.team.untitle.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReader {
    private final UserRepository userRepository;

    public User getUser(Long userId) {
        UserEntity entity = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        return new User(
                entity.getId(),
                entity.getRole()
        );
    }
}
