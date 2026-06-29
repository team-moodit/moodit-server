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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MatchUpWinnerResultManager {

    private final MatchResultRepository matchResultRepository;
    private final MatchPreferenceResultRepository preferenceResultRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public MatchResult getOrCreateMatchUpWinnerResult(Long matchId, Long userId) {
        // 1. 멱등성 검사
        Optional<MatchResultEntity> existingResult = matchResultRepository.findByUserIdAndMatchId(userId, matchId);
        if (existingResult.isPresent()) {
            MatchResultEntity res = existingResult.get();
            List<MatchPreferenceResultEntity> prefs = preferenceResultRepository.findByMatchResultId(res.getId());
            return toMatchResultDomain(res, prefs);
        }

        // 2. 호준님의 투표 원천 데이터 'MatchVoteCandidateEntity' 목록 로드
        List<MatchVoteCandidateEntity> votedCandidates = matchUpRepository.findVotedLabelsByMatchId(matchId);
        if (votedCandidates.isEmpty()) throw new ApiException(ErrorType.INVALID_REQUEST);

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_MATCH_NOT_FOUND));

        // 3. 결승전 대진표를 찾아서 진짜 최종 우승 사진 ID(winnerId) 뽑아오기
        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchIdWithLock(matchId);

        // 전체 대진 중 가장 마지막 라운드 번호를 가진 대진의 winnerId를 우승 사진 ID로 채택
        Long winnerPhotoId = matchUps.stream()
                .filter(MatchUpEntity::isVoted)
                .max(Comparator.comparingInt(MatchUpEntity::getRoundNumber))
                .map(MatchUpEntity::getWinnerId)
                .orElseThrow(() -> new ApiException(ErrorType.INVALID_REQUEST));

        // 4. 정교한 선호도 동점/박빙 알고리즘 분석 엔진 가동
        MatchPreferenceAnalysis analysis = MatchResultAnalyzer.analyze(votedCandidates);

        // getTotalImages() 대신 실제 필드인 initialImageCount 활용하여 라운드 계산
        int roundCount = match.getInitialImageCount() - 1;
        LocalDateTime now = LocalDateTime.now();

        // 5. MatchResultEntity 포맷에 자로 잰 듯이 바인딩하여 영속화
        MatchResultEntity resultEntity = matchResultRepository.save(new MatchResultEntity(
                match.getId(),
                userId,
                match.getTitle(),
                winnerPhotoId, //
                roundCount,    //
                now,
                analysis.getResultType(),
                analysis.getMainPref() != null ? PreferenceType.valueOf(analysis.getMainPref()) : null,
                analysis.getDetailPref() != null ? PreferenceDetailType.valueOf(analysis.getDetailPref()) : null
        ));

        // 6. 통계 랭킹 테이블 데이터 적재
        List<MatchPreferenceResultEntity> savedPrefs = analysis.getRanks().stream().map(rank ->
                preferenceResultRepository.save(new MatchPreferenceResultEntity(
                        resultEntity.getId(),
                        PreferenceType.valueOf(rank.getLabel()),
                        rank.getRank(),
                        rank.getCount()
                ))
        ).toList();

        // 7. 조회 도메인 객체(MatchResult) 포맷으로 조립하여 컴파일 최종 통과!
        return toMatchResultDomain(resultEntity, savedPrefs);
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
                                new PreferenceTypeScore(
                                        p.getPreferenceType(),
                                        p.getSelectedCount(),
                                        p.getRank()
                                )
                        ).toList()
                )
        );
    }
}