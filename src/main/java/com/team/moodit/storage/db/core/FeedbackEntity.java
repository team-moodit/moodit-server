package com.team.moodit.storage.db.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(
        name = "mission_feedback",
        indexes = {
                @Index(name = "udx_mission_feedback_user_mission_id", columnList = "userMissionId", unique = true)
        }
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedbackEntity extends BaseIdEntity {
    @Column(nullable = false)
    private Long userMissionId;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private double satisfactionScore;
    private String dissatisfactionReason;

    @CreationTimestamp
    private LocalDateTime submittedAt;

    public FeedbackEntity(Long userMissionId, Long userId, double satisfactionScore, String dissatisfactionReason) {
        this.userMissionId = userMissionId;
        this.userId = userId;
        this.satisfactionScore = satisfactionScore;
        this.dissatisfactionReason = dissatisfactionReason;
    }
}
