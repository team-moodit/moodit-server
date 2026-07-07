package com.team.moodit.domain.mission;

import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionTemplate {
    private Long id;
    private PreferenceType preferenceType;
    private PreferenceDetailType preferenceDetailType;
    private String title;
    private int displayOrder;
}
