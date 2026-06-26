package com.team.moodit.storage.db.core;

import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="`match_choice`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchChoiceEntity extends BaseNoStatusEntity{
    @Column(name = "match_up_id", nullable = false)
    private Long matchUpId;

    @Column(name = "photo_id", nullable = false)
    private Long photoId;

    //  이 reasonId가 바로 이미 데이터가 채워져 있는 match_vote 테이블의 id를 가리킵니다!
    @Column(name = "reason_id", nullable = false)
    private Long reasonId;

    public MatchChoiceEntity(Long matchUpId, Long photoId, Long reasonId) {
        if (matchUpId == null || photoId == null || reasonId == null) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
        this.matchUpId = matchUpId;
        this.photoId = photoId;
        this.reasonId = reasonId;
    }
}
