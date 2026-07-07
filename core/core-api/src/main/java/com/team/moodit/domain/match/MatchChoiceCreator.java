package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchChoiceEntity;
import com.team.moodit.storage.db.core.MatchChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchChoiceCreator {

    private final MatchChoiceRepository matchChoiceRepository;

    public MatchChoiceEntity createChoice(Long matchUpId, Long photoId, Long reasonId) {
        // 객체가 생성되는 시점에 이미 Not-Null 무결성이 완벽히 검증됩니다.
        MatchChoiceEntity choice = new MatchChoiceEntity(matchUpId, photoId, reasonId);
        return matchChoiceRepository.save(choice);
    }
}