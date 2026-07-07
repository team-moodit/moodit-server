package com.team.moodit.domain.user;

import com.team.moodit.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {
    private Long id;
    private UserRole role;
}
