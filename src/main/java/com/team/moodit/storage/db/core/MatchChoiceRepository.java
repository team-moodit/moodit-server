package com.team.moodit.storage.db.core;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchChoiceRepository extends JpaRepository<MatchChoiceEntity,Long> {
    List<MatchChoiceEntity> findByMatchUpId(Long matchUpId);
}
