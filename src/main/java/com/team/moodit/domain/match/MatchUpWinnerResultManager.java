package com.team.moodit.domain.match;

import com.team.moodit.domain.PreferenceTypeScore;
import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.storage.db.core.*;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchUpWinnerResultManager {

    private final MatchResultRepository matchResultRepository;
    private final MatchPreferenceResultRepository preferenceResultRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchRepository matchRepository;
    private final MatchResultAnalyzer analyzer; //  DI: 이제 인스턴스 메서드로 분석

    @Transactional
    public MatchResult getOrCreateMatchUpWinnerResult(Long matchId, Long userId) {
        // 1. 비관적 락을 적용한 멱등성 검사 (기존 find 대체)
        return matchResultRepository.findByUserIdAndMatchIdForUpdate(userId, matchId)
                .map(res -> toMatchResultDomain(res, preferenceResultRepository.findByMatchResultId(res.getId())))
                .orElseGet(() -> createMatchResult(matchId, userId));
    }

    private MatchResult createMatchResult(Long matchId, Long userId) {
        List<MatchVoteCandidateEntity> votedCandidates = matchUpRepository.findVotedLabelsByMatchId(matchId);
        if (votedCandidates.isEmpty()) throw new ApiException(ErrorType.INVALID_REQUEST);

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_MATCH_NOT_FOUND));

        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchIdWithLock(matchId);
        Long winnerPhotoId = matchUps.stream()
                .filter(MatchUpEntity::isVoted)
                .max(Comparator.comparingInt(MatchUpEntity::getRoundNumber))
                .map(MatchUpEntity::getWinnerId)
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_REQUEST));

        // 🎯 분석 엔진 호출 (인스턴스 메서드)
        MatchPreferenceAnalysis analysis = analyzer.analyze(votedCandidates);

        int totalMatchCount = match.getInitialImageCount() - 1; // 변수명 의미 명확화

        MatchResultEntity resultEntity = matchResultRepository.save(new MatchResultEntity(
                match.getId(),
                userId,
                match.getTitle(),
                winnerPhotoId,
                totalMatchCount,
                LocalDateTime.now(),
                analysis.getResultType(),
                analysis.getMainPref() != null ? PreferenceType.valueOf(analysis.getMainPref()) : null,
                analysis.getDetailPref() != null ? PreferenceDetailType.valueOf(analysis.getDetailPref()) : null
        ));

        //  saveAll() 최적화: 랭킹 엔티티 리스트를 한 번에 저장
        List<MatchPreferenceResultEntity> preferenceEntities = analysis.getRanks().stream()
                .map(rank -> new MatchPreferenceResultEntity(
                        resultEntity.getId(),
                        PreferenceType.valueOf(rank.getLabel()),
                        rank.getRank(),
                        rank.getCount()
                ))
                .toList();
        preferenceResultRepository.saveAll(preferenceEntities);

        return toMatchResultDomain(resultEntity, preferenceEntities);
    }

    private MatchResult toMatchResultDomain(MatchResultEntity entity, List<MatchPreferenceResultEntity> preferenceEntities) {
        return new MatchResult(
                entity.getId(),
                entity.getMatchId(),
                entity.getTitle(),
                entity.getRepresentativeMatchImageId(),
                entity.getRoundCount(),
                entity.getCompletedAt(),
                new MatchPreferenceResult(
                        entity.getPreferenceResultType(),
                        entity.getPreferenceType(),
                        entity.getPreferenceDetailType(),
                        preferenceEntities.stream().map(p ->
                                new PreferenceTypeScore(p.getPreferenceType(), p.getSelectedCount(), p.getRank())
                        ).toList()
                )
        );
    }
}