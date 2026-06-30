package com.team.moodit.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrivacy {
    private Long id;
    private Long userId;
    private String name;
    private String email;
}
