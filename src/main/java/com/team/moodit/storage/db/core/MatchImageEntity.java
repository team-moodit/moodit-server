package com.team.moodit.storage.db.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "match_image")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchImageEntity extends BaseNoStatusEntity {

    private Long matchId;

    @Column(name = "file_id")
    private Long imageId;


    // TODO: 필요 시 추가 필드 추가 예정
}
