package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor //
public class MatchUpCreateResult {

    private final List<MatchUpEntity> matchUps;
    private final List<MatchVoteCandidateEntity> voteCandidates;

}