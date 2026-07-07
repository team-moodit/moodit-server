package com.team.moodit.domain.missionOffer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MissionCandidate {
    private Long id;
    private Long offerId;
    private Long missionTemplateId;
    private String title;
    private int displayOrder;
}
