package com.team.moodit.domain.user;

public interface UserWithdrawalPostProcessor {
    void process(Long userId);
}
