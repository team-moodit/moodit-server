package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.UserMissionState;
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
        name = "user_mission",
        indexes = {
                @Index(name = "udx_user_mission_mission_offer_id", columnList = "missionOfferId", unique = true)
        }
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMissionEntity extends BaseEntity {
    private Long userId;
    private Long matchId;
    private Long missionOfferId;
    private Long missionTemplateId;
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private UserMissionState state;
    private LocalDateTime completedAt;

    public void completed() {
        this.state = UserMissionState.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void reviewed() {
        this.state = UserMissionState.REVIEWED;
    }

    public UserMissionEntity(Long userId, Long matchId, Long missionOfferId, Long missionTemplateId, String title, UserMissionState state) {
        this.userId = userId;
        this.matchId = matchId;
        this.missionOfferId = missionOfferId;
        this.missionTemplateId = missionTemplateId;
        this.title = title;
        this.state = state;
    }
}
