package com.team.moodit.storage.db.core;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
public class FileEntity extends BaseIdEntity {
    private Long userId;
    private String objectKey;
    private String originalName;
    private String contentType;
    private Long size;

    public FileEntity(Long userId, String objectKey, String originalName, String contentType, Long size) {
        this.userId = userId;
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.contentType = contentType;
        this.size = size;
    }

    @CreationTimestamp
    private LocalDateTime createdAt;
}
