package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.SocialProviderType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_auth_identity",
        indexes = {
                @Index(name = "udx_user_auth_identity_provider_type_provider_user_id", columnList = "providerType, providerUserId")
        }
)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthIdentityEntity extends BaseNoStatusEntity {
    private Long userId;
    @Enumerated(EnumType.STRING)
    private SocialProviderType providerType;
    private String providerUserId;
}
