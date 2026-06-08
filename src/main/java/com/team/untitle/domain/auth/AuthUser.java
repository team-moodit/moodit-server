package com.team.untitle.domain.auth;

import com.team.untitle.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {
    private Long id;
    private UserRole role;
}
