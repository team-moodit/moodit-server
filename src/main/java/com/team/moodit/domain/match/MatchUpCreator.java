package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import com.team.moodit.storage.db.core.MatchVoteEntity;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MatchUpCreator {

    public MatchUpCreateResult createMatches(Long matchId, List<Long> imageIds, List<MatchVoteEntity> allTemplates) {
        if (imageIds == null || imageIds.size() < 8 || imageIds.size() > 32) {
            throw new ApiException(ErrorType.INVALID_IMAGE_COUNT);
        }

        int totalImages = imageIds.size();
        int targetRound = calculateTargetRound(totalImages);
        int matchCount = totalImages - targetRound;
        int firstRoundPlayersCount = matchCount * 2;
        int totalMatchRounds = totalImages - 1;

        List<Long> shuffledIds = new ArrayList<>(imageIds);
        Collections.shuffle(shuffledIds);

        List<MatchUpEntity> matchUps = new ArrayList<>();

        // 예선전 대결 매치업 등록 (NEED_VOTE)
        for (int i = 0; i < firstRoundPlayersCount; i += 2) {
            matchUps.add(MatchUpEntity.of(
                    new RealMatchUp(matchId, totalImages, shuffledIds.get(i), shuffledIds.get(i + 1))
            ));
        }

        // 부전승 자동 진출 등록 (SKIPPED)
        for (int i = firstRoundPlayersCount; i < totalImages; i++) {
            matchUps.add(MatchUpEntity.of(
                    new AutoPassMatch(matchId, totalImages, shuffledIds.get(i))
            ));
        }

        List<MatchVoteCandidateEntity> voteCandidates = new ArrayList<>();

        if (allTemplates != null && !allTemplates.isEmpty()) {
            List<MatchVoteEntity> bodyFits = new ArrayList<>();
            List<MatchVoteEntity> colors = new ArrayList<>();
            List<MatchVoteEntity> vibes = new ArrayList<>();
            List<MatchVoteEntity> designs = new ArrayList<>();
            List<MatchVoteEntity> matchables = new ArrayList<>();
            List<MatchVoteEntity> moods = new ArrayList<>();
            List<MatchVoteEntity> consistences = new ArrayList<>();
            List<MatchVoteEntity> trends = new ArrayList<>();

            //  DB 문자열 매칭 안정성을 위해 trim() 및 대문자 안전 변환 적용
            for (MatchVoteEntity t : allTemplates) {
                String pref = t.getPreference() != null ? t.getPreference().trim().toUpperCase() : "";
                String detail = t.getPreferenceDetail() != null ? t.getPreferenceDetail().trim().toUpperCase() : "";

                if ("CONSISTENCE".equals(pref)) consistences.add(t);
                else if ("TREND".equals(pref)) trends.add(t);
                else if ("FITNESS".equals(pref) && "BODY_FIT".equals(detail)) bodyFits.add(t);
                else if ("AESTHETICS".equals(pref) && "COLOR".equals(detail)) colors.add(t);
                else if ("FITNESS".equals(pref) && "VIBE".equals(detail)) vibes.add(t);
                else if ("AESTHETICS".equals(pref) && "DESIGN".equals(detail)) designs.add(t);
                else if ("FITNESS".equals(pref) && "MATCHABLE".equals(detail)) matchables.add(t);
                else if ("AESTHETICS".equals(pref) && "MOOD".equals(detail)) moods.add(t);
            }

            //  [핵심 방어코드] 특정 풀이 비어있을 경우 전체 풀을 백업으로 활용하여 IndexOutOfBounds 방지
            if (bodyFits.isEmpty()) bodyFits.addAll(allTemplates);
            if (colors.isEmpty()) colors.addAll(allTemplates);
            if (vibes.isEmpty()) vibes.addAll(allTemplates);
            if (designs.isEmpty()) designs.addAll(allTemplates);
            if (matchables.isEmpty()) matchables.addAll(allTemplates);
            if (moods.isEmpty()) moods.addAll(allTemplates);
            if (consistences.isEmpty()) consistences.addAll(allTemplates);
            if (trends.isEmpty()) trends.addAll(allTemplates);

            //  [보정 반영] 결승전이 아닌 일반 라운드만 순차적으로 카운트할 축(Index) 선언
            int normalRoundIdx = 1;

            for (int round = 1; round <= totalMatchRounds; round++) {
                Collections.shuffle(bodyFits);     Collections.shuffle(colors);
                Collections.shuffle(vibes);        Collections.shuffle(designs);
                Collections.shuffle(matchables);   Collections.shuffle(moods);
                Collections.shuffle(consistences); Collections.shuffle(trends);

                MatchVoteEntity r1 = null;
                MatchVoteEntity r2 = null;
                MatchVoteEntity r3 = consistences.get(0);
                MatchVoteEntity r4 = trends.get(0);

                boolean isFinalRound = (round == totalMatchRounds);

                if (isFinalRound) {
                    List<MatchVoteEntity> fitnessPool = new ArrayList<>();
                    fitnessPool.addAll(bodyFits); fitnessPool.addAll(vibes); fitnessPool.addAll(matchables);
                    Collections.shuffle(fitnessPool);
                    r1 = fitnessPool.get(0);

                    List<MatchVoteEntity> aestheticsPool = new ArrayList<>();
                    aestheticsPool.addAll(colors); aestheticsPool.addAll(designs); aestheticsPool.addAll(moods);
                    Collections.shuffle(aestheticsPool);
                    r2 = aestheticsPool.get(0);
                } else {
                    // round 대신 normalRoundIdx를 기준으로 3주기 패턴 분배
                    int patternIdx = normalRoundIdx % 3;

                    if (patternIdx == 1) {
                        // 1번째 칼럼 세트: 신체적특징(분홍색) + 색감(연두색) 수직 정렬 매칭
                        r1 = bodyFits.get(0);
                        r2 = colors.get(0);
                    } else if (patternIdx == 2) {
                        // 2번째 칼럼 세트: 추구미 + 디자인
                        r1 = vibes.get(0);
                        r2 = designs.get(0);
                    } else {
                        // 3번째 칼럼 세트: 코디용이성 + 분위기
                        r1 = matchables.get(0);
                        r2 = moods.get(0);
                    }

                    normalRoundIdx++; // 일반 라운드 카운트 증가
                }

                if (r1 != null) voteCandidates.add(new MatchVoteCandidateEntity(matchId, round, r1.getId(), r1.getContent(), r1.getPreference(), r1.getPreferenceDetail()));
                if (r2 != null) voteCandidates.add(new MatchVoteCandidateEntity(matchId, round, r2.getId(), r2.getContent(), r2.getPreference(), r2.getPreferenceDetail()));
                if (r3 != null) voteCandidates.add(new MatchVoteCandidateEntity(matchId, round, r3.getId(), r3.getContent(), r3.getPreference(), r3.getPreferenceDetail()));
                if (r4 != null) voteCandidates.add(new MatchVoteCandidateEntity(matchId, round, r4.getId(), r4.getContent(), r4.getPreference(), r4.getPreferenceDetail()));
            }
        }

        return new MatchUpCreateResult(matchUps, voteCandidates);
    }

    public List<MatchUpEntity> createNextRoundMatches(Long matchId, List<Long> winnerImageIds) {
        if (winnerImageIds == null || winnerImageIds.isEmpty() || winnerImageIds.size() % 2 != 0) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        int nextRoundNumber = winnerImageIds.size();
        List<Long> shuffledIds = new ArrayList<>(winnerImageIds);
        Collections.shuffle(shuffledIds);

        List<MatchUpEntity> nextMatchUps = new ArrayList<>();

        for (int i = 0; i < shuffledIds.size(); i += 2) {
            nextMatchUps.add(MatchUpEntity.of(
                    new RealMatchUp(matchId, nextRoundNumber, shuffledIds.get(i), shuffledIds.get(i + 1))
            ));
        }

        return nextMatchUps;
    }

    private int calculateTargetRound(int n) {
        if (n <= 0) return 0;
        return Integer.highestOneBit(n - 1);
    }
}