package com.team.moodit.domain.auth;

import com.team.moodit.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {
    private Long id;
    private UserRole role;
}
