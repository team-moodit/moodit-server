package com.team.moodit.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfile {
    private Long id;
    private Long userId;
    private String email;
    private String nickname;
}
