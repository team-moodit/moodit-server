package com.team.moodit.storage.db.core;

import com.team.moodit.domain.enums.ObjectResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Entity
@Table(name = "file")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileEntity extends BaseNoStatusEntity {
    private Long userId;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR")
    private ObjectResourceType resourceType;
    private String objectKey;
    private String originalName;
    private String contentType;
    private Long byteSize;

    public FileEntity(Long userId, ObjectResourceType resourceType, String objectKey, String originalName,
                      String contentType) {
        this.userId = userId;
        this.resourceType = resourceType;
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.contentType = contentType;
    }
}
