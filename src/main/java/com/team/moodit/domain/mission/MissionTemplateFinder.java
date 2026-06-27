package com.team.moodit.domain.mission;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.domain.match.MatchPreferenceResult;
import com.team.moodit.storage.db.core.MissionTemplateEntity;
import com.team.moodit.storage.db.core.MissionTemplateRepository;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionTemplateFinder {
    private final MissionTemplateRepository missionTemplateRepository;

    public List<MissionTemplate> findOfferable(MatchPreferenceResult result) {
        return findOfferableEntities(result).stream()
                .map(this::toMissionTemplate)
                .toList();
    }

    public List<MissionTemplate> findRandomByPreferenceTypes(MatchPreferenceResult result) {
        List<PreferenceType> preferenceTypes = fallbackPreferenceTypes(result);
        return pickOneByPreferenceType(
                missionTemplateRepository.findByPreferenceTypeInAndStatus(preferenceTypes, EntityStatus.ACTIVE)
        ).stream()
                .map(this::toMissionTemplate)
                .toList();
    }

    private List<PreferenceType> fallbackPreferenceTypes(MatchPreferenceResult result) {
        List<PreferenceType> topRankPreferenceTypes = result.getTopRankPreferenceType();
        if (!topRankPreferenceTypes.isEmpty()) {
            return topRankPreferenceTypes;
        }
        if (result.getPreferenceType() != null) {
            return List.of(result.getPreferenceType());
        }
        return Arrays.stream(PreferenceType.values()).toList();
    }

    private List<MissionTemplateEntity> findOfferableEntities(MatchPreferenceResult result) {
        return switch (result.getResultType()) {
            case TYPE_AND_DETAIL -> findByTypeAndDetail(result);
            case TYPE_ONLY -> findByType(result);
            case TIE -> findTieOfferable(result);
        };
    }

    private List<MissionTemplateEntity> findByTypeAndDetail(MatchPreferenceResult result) {
        return missionTemplateRepository.findByPreferenceTypeAndPreferenceDetailTypeAndStatus(
                result.getPreferenceType(),
                result.getPreferenceDetailType(),
                EntityStatus.ACTIVE
        );
    }

    private List<MissionTemplateEntity> findByType(MatchPreferenceResult result) {
        return missionTemplateRepository.findByPreferenceTypeAndStatus(
                result.getPreferenceType(),
                EntityStatus.ACTIVE
        );
    }

    private List<MissionTemplateEntity> findTieOfferable(MatchPreferenceResult result) {
        return pickOneByPreferenceType(
                missionTemplateRepository.findByPreferenceTypeInAndStatus(
                        result.getTopRankPreferenceType(),
                        EntityStatus.ACTIVE
                )
        );
    }

    private MissionTemplate toMissionTemplate(MissionTemplateEntity entity) {
        return new MissionTemplate(
                entity.getId(),
                entity.getPreferenceType(),
                entity.getPreferenceDetailType(),
                entity.getTitle(),
                entity.getDisplayOrder()
        );
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
