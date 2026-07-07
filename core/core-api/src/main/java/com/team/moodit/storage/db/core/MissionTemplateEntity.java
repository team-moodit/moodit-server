package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "mission_template")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionTemplateEntity extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private PreferenceType preferenceType;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private PreferenceDetailType preferenceDetailType;
    private String title;
    private int displayOrder;
}
