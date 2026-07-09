package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.support.OffsetLimit;
import com.team.moodit.support.Page;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchFinder {
    private final MatchRepository matchRepository;
    private final MatchUpRepository matchUpRepository;

    public Page<Match> find(Long userId, MatchState state, OffsetLimit offsetLimit) {
        org.springframework.data.domain.Page<MatchEntity> result = matchRepository.findByUserIdAndStateOrderByCreatedAt(
                userId,
                state,
                offsetLimit.toPageable()
        );

        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchIdIn(result.getContent().stream().map(MatchEntity::getId).toList());
        MatchUpEntity finalMatchUp = matchUps.stream().sorted(Comparator.comparing(MatchUpEntity::getRoundNumber).reversed()).toList().getFirst();

        return new Page<>(
                result.getContent().stream().map(it ->
                        new Match(
                                it.getId(),
                                it.getUserId(),
                                finalMatchUp.getWinnerId(),
                                it.getTitle(),
                                it.getState(),
                                finalMatchUp.getWinnerId() != null ? finalMatchUp.getUpdatedAt() : null
                        )
                ).toList(),
                result.getTotalElements(),
                result.hasNext()
        );
    }
}
