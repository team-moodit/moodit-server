package com.team.moodit.domain.mission;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.match.MatchPreferenceResult;
import com.team.moodit.storage.db.core.MissionTemplateEntity;
import com.team.moodit.storage.db.core.MissionTemplateRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionTemplateFinder {
    private final MissionTemplateRepository missionTemplateRepository;

    public List<MissionTemplate> findOfferable(MatchPreferenceResult result) {
        List<MissionTemplateEntity> entities = switch (result.getResultType()) {
            case TYPE_AND_DETAIL -> missionTemplateRepository.findByPreferenceTypeAndPreferenceDetailTypeAndStatus(
                    result.getPreferenceType(),
                    result.getPreferenceDetailType(),
                    EntityStatus.ACTIVE
            );
            case TYPE_ONLY -> missionTemplateRepository.findByPreferenceTypeAndStatus(result.getPreferenceType(), EntityStatus.ACTIVE);
            case TIE -> pickOneByPreferenceType(
                    missionTemplateRepository.findByPreferenceTypeInAndStatus(result.getTopRankPreferenceType(), EntityStatus.ACTIVE)
            );
        };

        if (entities.isEmpty()) {
            log.error(
                    "[MissionTemplateFinder] 제안 가능한 미션이 없습니다. Mission 기준 데이터를 확인해주세요. resultType: {}, preferenceType: {}, preferenceDetailType: {}, topRankPreferenceTypes: {}",
                    result.getResultType(),
                    result.getPreferenceType(),
                    result.getPreferenceDetailType(),
                    result.getTopRankPreferenceType()
            );
            throw new ApiException(ErrorType.DEFAULT_ERROR);
        }

        return entities.stream().map(it ->
                new MissionTemplate(
                        it.getId(),
                        it.getPreferenceType(),
                        it.getPreferenceDetailType(),
                        it.getTitle(),
                        it.getDisplayOrder()
                )
        ).toList();
    }

    // 선호 결과가 동률이 나와 도출되지 않았을 때 선호 타입 미션 중 랜덤 하나씩 뽑아내기 위함
    private List<MissionTemplateEntity> pickOneByPreferenceType(List<MissionTemplateEntity> entities) {
        return entities.stream()
                .collect(Collectors.groupingBy(MissionTemplateEntity::getPreferenceType))
                .values()
                .stream()
                .map(it -> it.get(ThreadLocalRandom.current().nextInt(it.size())))
                .toList();
    }
}
