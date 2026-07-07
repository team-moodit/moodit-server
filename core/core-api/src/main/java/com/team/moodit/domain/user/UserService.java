package com.team.moodit.domain.user;

import com.team.moodit.support.auth.ApiUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final List<UserWithdrawalPostProcessor> processor;

    @Transactional
    public Long withdraw(ApiUser apiUser) {
        processor.forEach(it -> it.process(apiUser.getId()));
        return apiUser.getId();
    }
}
