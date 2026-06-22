package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.MatchState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@Table(name = "match")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchEntity extends BaseNoStatusEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MatchState state;


    @Column(name = "initial_image_count", nullable = false)
    private Integer initialImageCount;





}
