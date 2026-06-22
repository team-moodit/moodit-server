package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.match.MatchResultReader;
import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.mission.MissionTemplate;
import com.team.moodit.domain.mission.MissionTemplateFinder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionOfferService {
    private final MatchResultReader matchResultReader;
    private final MissionTemplateFinder missionTemplateFinder;
    private final MissionOfferCreator missionOfferCreator;
    private final MissionOfferAcceptHandler missionOfferAcceptHandler;

    public MissionOffer createOffer(Long userId, Long matchId) {
        MatchResult matchResult = matchResultReader.getMatchResult(userId, matchId);
        List<MissionTemplate> missionTemplates = missionTemplateFinder.findOfferable(matchResult.getPreferenceResult());

        // 제안 미션이 한 개 -> 바로 할당
        if (missionTemplates.size() == 1) {
            return missionOfferAcceptHandler.createAcceptedOfferAndMission(
                    userId,
                    matchResult,
                    missionTemplates.getFirst()
            );
        }

        // 제안 미션이 여러 개
        return missionOfferCreator.createSelectionOffer(userId, matchResult, missionTemplates);
    }
}
