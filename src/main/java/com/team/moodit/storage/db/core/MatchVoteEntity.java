package com.team.moodit.storage.db.core;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_vote") // DB에 생성된 실제 테이블명 매핑
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 규칙 적용
public class MatchVoteEntity extends BaseIdEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 예: "유행을 타지 않을 것 같아서", "클래식한 느낌이라서"

    @Column(nullable = false)
    private String preference; // 예: "CONSISTENCE", "FITNESS"

    private String preferenceDetail; // 예: null, "VIBE", "MATCHABLE" (null 허용)
}