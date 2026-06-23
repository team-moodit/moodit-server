package com.team.moodit.storage.db.core;

import com.team.moodit.domain.match.MatchImage;
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
    private Long imageId;

    public MatchImageEntity(MatchImage matchImage) {
        this.matchId = matchImage.getMatchId();
        this.imageId = matchImage.getImageId();
    }

    public MatchImage toDomain() {
        return new MatchImage(
                this.matchId,
                this.imageId
        );
    }

}

