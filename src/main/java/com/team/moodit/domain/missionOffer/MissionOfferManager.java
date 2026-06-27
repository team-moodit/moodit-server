package com.team.moodit.domain.missionOffer;

import com.team.moodit.domain.match.MatchResult;
import com.team.moodit.domain.mission.MissionTemplate;
import com.team.moodit.domain.mission.MissionTemplateFinder;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionOfferManager {
    private final MissionOfferReader missionOfferReader;
    private final MissionTemplateFinder missionTemplateFinder;
    private final MissionOfferCreator missionOfferCreator;
    private final MissionOfferAcceptHandler missionOfferAcceptHandler;

    public MissionOfferCreateResult getOrCreate(Long userId, MatchResult matchResult) {
        return missionOfferReader
                .findCreateResultByUserIdAndMatchResultId(userId, matchResult.getId())
                .orElseGet(() -> createOrRead(userId, matchResult));
    }

    private MissionOfferCreateResult createOrRead(Long userId, MatchResult matchResult) {
        try {
            return create(userId, matchResult);
        } catch (DataIntegrityViolationException e) {
            log.info(
                    "[MissionOfferManager] 이미 생성된 미션 제안을 재조회합니다. userId: {}, matchResultId: {}",
                    userId,
                    matchResult.getId()
            );
            return missionOfferReader
                    .findCreateResultByUserIdAndMatchResultId(userId, matchResult.getId())
                    .orElseThrow(() -> e);
        }
    }

    private MissionOfferCreateResult create(Long userId, MatchResult matchResult) {
        List<MissionTemplate> missionTemplates = missionTemplateFinder.findOfferable(matchResult.getPreferenceResult());
        if (missionTemplates.isEmpty()) {
            log.error(
                    "[MissionOfferManager] 제안 가능한 미션이 없어 fallback 미션을 제공합니다. userId: {}, matchResultId: {}",
                    userId,
                    matchResult.getId()
            );
            missionTemplates = missionTemplateFinder.findRandomByPreferenceTypes(matchResult.getPreferenceResult());
        }

        if (missionTemplates.isEmpty()) {
            log.error(
                    "[MissionOfferManager] fallback 미션도 찾지 못했습니다. userId: {}, matchResultId: {}",
                    userId,
                    matchResult.getId()
            );
            throw new ApiException(ErrorType.DEFAULT_ERROR);
        }

        // 제안 가능한 미션이 한 개라면 자동 할당
        if (missionTemplates.size() == 1) {
            return missionOfferAcceptHandler.createAcceptedOfferAndMission(
                    userId,
                    matchResult,
                    missionTemplates.getFirst()
            );
        }

        MissionOffer missionOffer = missionOfferCreator.createSelectionOffer(
                userId,
                matchResult,
                missionTemplates
        );

        return MissionOfferCreateResult.selection(missionOffer);
    }
}
