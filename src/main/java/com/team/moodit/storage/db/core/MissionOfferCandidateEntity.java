package com.team.moodit.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "mission_offer_candidate")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionOfferCandidateEntity extends BaseNoStatusEntity {
    private long offerId;
    private long missionTemplateId;
    private String title;
    private int displayOrder;
}
