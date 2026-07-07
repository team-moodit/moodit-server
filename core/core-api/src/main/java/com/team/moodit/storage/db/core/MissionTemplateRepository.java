package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionTemplateRepository extends JpaRepository<MissionTemplateEntity, Long> {
    List<MissionTemplateEntity> findByPreferenceTypeAndPreferenceDetailTypeAndStatus(PreferenceType preferenceType, PreferenceDetailType preferenceDetailType, EntityStatus status);
    List<MissionTemplateEntity> findByPreferenceTypeAndStatus(PreferenceType preferenceType, EntityStatus status);
    List<MissionTemplateEntity> findByPreferenceTypeInAndStatus(List<PreferenceType> preferenceTypes, EntityStatus status);
    List<MissionTemplateEntity> findByPreferenceTypeAndPreferenceDetailTypeInAndStatus(
            PreferenceType preferenceType,
            List<PreferenceDetailType> preferenceDetailTypes,
            EntityStatus status
    );
}
