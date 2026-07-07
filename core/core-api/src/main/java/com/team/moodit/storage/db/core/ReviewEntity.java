package com.team.moodit.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(
        name = "review",
        indexes = {
                @Index(name = "udx_user_review", columnList = "userId, userMissionId", unique = true)
        }
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewEntity extends BaseIdEntity {
    private Long userId;
    private Long userMissionId;
    private BigDecimal rate;
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public ReviewEntity(Long userId, Long userMissionId, BigDecimal rate, String content) {
        this.userId = userId;
        this.userMissionId = userMissionId;
        this.rate = rate;
        this.content = content;
    }
}
