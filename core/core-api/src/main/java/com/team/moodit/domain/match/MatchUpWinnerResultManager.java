package com.team.moodit.domain.match;

import com.team.moodit.domain.PreferenceDetailTypeScore;
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
    private final MatchChoiceRepository matchChoiceRepository; //
    private final MatchResultAnalyzer analyzer;

    @Transactional
    public MatchResult getOrCreateMatchUpWinnerResult(Long matchId, Long userId) {
        // 1. 비관적 락을 적용한 멱등성 검사
        return matchResultRepository.findByUserIdAndMatchIdForUpdate(userId, matchId)
                .map(res -> toMatchResultDomain(res, preferenceResultRepository.findByMatchResultId(res.getId())))
                .orElseGet(() -> createMatchResult(matchId, userId));
    }

    private MatchResult createMatchResult(Long matchId, Long userId) {

        List<MatchVoteCandidateEntity> votedCandidates = matchChoiceRepository.findActualVotedCandidatesByMatchId(matchId);
        if (votedCandidates == null || votedCandidates.isEmpty()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_MATCH_NOT_FOUND));

        // 3. 결승전 대진표에서 최종 우승 사진 ID 추출
        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);
        Long winnerPhotoId = matchUps.stream()
                .filter(MatchUpEntity::isVoted)
                .max(Comparator.comparingInt(MatchUpEntity::getRoundNumber))
                .map(MatchUpEntity::getWinnerId)
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_REQUEST));

        // 4. 선호도 분석 엔진 호출 (이제 유저의 실데이터가 공급되어 TIE가 풀립니다)
        MatchPreferenceAnalysis analysis = analyzer.analyze(votedCandidates);

        int totalMatchCount = match.getInitialImageCount() - 1;

        // 5. 결과 엔티티 저장
        MatchResultEntity resultEntity = matchResultRepository.save(new MatchResultEntity(
                match.getId(),
                userId,
                match.getTitle(),
                winnerPhotoId,
                totalMatchCount,
                LocalDateTime.now(),
                analysis.getResultType(),
                safeToPreferenceType(analysis.getMainPref()),
                safeToPreferenceDetailType(analysis.getDetailPref())
        ));



        // 6. 랭킹 엔티티 리스트 한 번에 저장
        List<MatchPreferenceResultEntity> preferenceEntities = analysis.getRanks().stream()
                .map(rank -> new MatchPreferenceResultEntity(
                        resultEntity.getId(),
                        safeToPreferenceType(rank.getLabel()),
                        rank.getPreferenceDetailType(),
                        rank.getCount(),
                        rank.getRank()
                ))
                .toList();
        preferenceResultRepository.saveAll(preferenceEntities);

        // 7. 도메인 객체로 조립하여 반환
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
                                new PreferenceTypeScore(p.getPreferenceType(),p.getPreferenceDetailType(), p.getSelectedCount(), p.getRank())
                        ).toList(),
                        preferenceEntities.stream().map(it ->
                                new PreferenceDetailTypeScore(it.getPreferenceType(), it.getPreferenceDetailType(), it.getSelectedCount(), it.getRank())
                        ).toList()
                )
        );
    }

    private PreferenceType safeToPreferenceType(String value) {
        if (value == null) return null;
        for (PreferenceType type : PreferenceType.values()) {
            if (type.name().equals(value)) return type;
        }
        throw new ApiException(ErrorType.INVALID_REQUEST);
    }

    private PreferenceDetailType safeToPreferenceDetailType(String value) {
        if (value == null) return null;
        for (PreferenceDetailType type : PreferenceDetailType.values()) {
            if (type.name().equals(value)) return type;
        }
        throw new ApiException(ErrorType.INVALID_REQUEST);
    }
}
