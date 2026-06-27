package com.team.moodit.storage.db.core;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "`match_vote_candidate`",
        indexes = {
                //  여기에 match_id와 round_number 복합 인덱스를 코드로 지정합니다!
                @Index(name = "idx_match_vote_candidate_match_round", columnList = "match_id, round_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MatchVoteCandidateEntity extends BaseIdEntity {

    @Column(nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private Integer roundNumber;

    @Column(nullable = false)
    private Long voteId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String preference;

    @Column(nullable = true)
    private String preferenceDetail;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public MatchVoteCandidateEntity(Long matchId, Integer roundNumber, Long voteId, String content, String preference, String preferenceDetail) {
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.voteId = voteId;
        this.content = content;
        this.preference = preference;
        this.preferenceDetail = preferenceDetail;
        this.createdAt = LocalDateTime.now();
    }
}