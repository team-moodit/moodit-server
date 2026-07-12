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

    public MissionOfferCreateResult getOrCreate(Long userId, MatchResult matchResult) {
        return missionOfferReader.findCreateResultByUserIdAndMatchResultId(
                userId,
                matchResult.getId(),
                matchResult.getPreferenceResult().getResultType()
        ).orElseGet(() -> createOrRead(userId, matchResult));
    }

    private MissionOfferCreateResult createOrRead(Long userId, MatchResult matchResult) {
        try {
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

            MissionOffer missionOffer = missionOfferCreator.createOffer(
                    userId,
                    matchResult,
                    missionTemplates
            );

            return MissionOfferCreateResult.of(
                    missionOffer,
                    matchResult.getPreferenceResult().getResultType(),
                    null
            );
        } catch (DataIntegrityViolationException e) {
            log.info(
                    "[MissionOfferManager] 이미 생성된 미션 제안을 재조회합니다. userId: {}, matchResultId: {}",
                    userId,
                    matchResult.getId()
            );
            return missionOfferReader.findCreateResultByUserIdAndMatchResultId(
                    userId,
                    matchResult.getId(),
                    matchResult.getPreferenceResult().getResultType()
            ).orElseThrow(() -> e);
        }
    }
}
