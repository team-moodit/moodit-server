package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MissionOfferState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "mission_offer",
        indexes = {
                @Index(name = "udx_mission_offer_match_id_user_id", columnList = "matchId, userId", unique = true)
        }
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionOfferEntity extends BaseNoStatusEntity {
    private Long matchId;
    private Long userId;
    private Long acceptedCandidateId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private MissionOfferState state;
    private LocalDateTime acceptedAt;

    public void accepted(Long acceptedCandidateId) {
        this.acceptedCandidateId = acceptedCandidateId;
        this.state = MissionOfferState.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public MissionOfferEntity(Long matchId, Long userId, MissionOfferState state) {
        this.matchId = matchId;
        this.userId = userId;
        this.state = state;
    }
}
