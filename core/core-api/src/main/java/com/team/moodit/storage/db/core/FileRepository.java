package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByUserIdAndIdIn(Long userId, List<Long> imageIds);
}
