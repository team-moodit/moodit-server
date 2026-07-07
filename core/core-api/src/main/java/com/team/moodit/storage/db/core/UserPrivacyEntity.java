package com.team.moodit.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_privacy",
        indexes = {
                @Index(name = "udx_user_privacy_user_id", columnList = "userId", unique = true)
        }
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPrivacyEntity extends BaseNoStatusEntity {
    private Long userId;
    private String name;
    private String email;

    public void applyName(String name) {
        this.name = name;
    }
}
