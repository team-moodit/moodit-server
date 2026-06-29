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
        if (allTemplates == null || allTemplates.isEmpty()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        int totalImages = imageIds.size();
        int targetRound = calculateTargetRound(totalImages);
        int matchCount = totalImages - targetRound;
        int firstRoundPlayersCount = matchCount * 2;
        int totalMatchRounds = totalImages - 1;

        List<Long> shuffledIds = new ArrayList<>(imageIds);
        Collections.shuffle(shuffledIds);

        List<MatchUpEntity> matchUps = new ArrayList<>(totalImages); // 🚀 Capacity 지정으로 배열 재할당 방지
        int firstRoundNumber = 1;

        for (int i = 0; i < firstRoundPlayersCount; i += 2) {
            matchUps.add(MatchUpEntity.of(
                    new RealMatchUp(matchId, firstRoundNumber, shuffledIds.get(i), shuffledIds.get(i + 1))
            ));
        }

        for (int i = firstRoundPlayersCount; i < totalImages; i++) {
            matchUps.add(MatchUpEntity.of(
                    new AutoPassMatch(matchId, firstRoundNumber, shuffledIds.get(i))
            ));
        }

        // 예상 질문 후보군 크기만큼 ArrayList 초기 용량 미리 확보 (메모리 최적화)
        // 판당 최대 4개의 질문이 생성되므로 예측 가능
        List<MatchVoteCandidateEntity> voteCandidates = new ArrayList<>(totalMatchRounds * 4);

        List<MatchVoteEntity> bodyFits = new ArrayList<>();
        List<MatchVoteEntity> colors = new ArrayList<>();
        List<MatchVoteEntity> vibes = new ArrayList<>();
        List<MatchVoteEntity> designs = new ArrayList<>();
        List<MatchVoteEntity> matchables = new ArrayList<>();
        List<MatchVoteEntity> moods = new ArrayList<>();
        List<MatchVoteEntity> consistences = new ArrayList<>();
        List<MatchVoteEntity> trends = new ArrayList<>();

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

        // 방어벽 작동
        if (bodyFits.isEmpty()) bodyFits.addAll(allTemplates);
        if (colors.isEmpty()) colors.addAll(allTemplates);
        if (vibes.isEmpty()) vibes.addAll(allTemplates);
        if (designs.isEmpty()) designs.addAll(allTemplates);
        if (matchables.isEmpty()) matchables.addAll(allTemplates);
        if (moods.isEmpty()) moods.addAll(allTemplates);
        if (consistences.isEmpty()) consistences.addAll(allTemplates);
        if (trends.isEmpty()) trends.addAll(allTemplates);

        // [최적화] shuffle은 루프 밖에서 딱 한 번만 수행!
        // 한 번 무작위로 섞인 리스트를 인덱스 순환으로 타기 때문에 무작위성은 완벽하게 유지됩니다.
        Collections.shuffle(bodyFits);     Collections.shuffle(colors);
        Collections.shuffle(vibes);        Collections.shuffle(designs);
        Collections.shuffle(matchables);   Collections.shuffle(moods);
        Collections.shuffle(consistences); Collections.shuffle(trends);

        int bodyFitIdx = 0;   int colorIdx = 0;
        int vibeIdx = 0;      int designIdx = 0;
        int matchableIdx = 0; int moodIdx = 0;
        int consistenceIdx = 0; int trendIdx = 0;

        int normalRoundIdx = 1;
        MatchVoteEntity defaultBackup = allTemplates.get(0);

        for (int round = 1; round <= totalMatchRounds; round++) {
            MatchVoteEntity r1 = null;
            MatchVoteEntity r2 = null;

            MatchVoteEntity r3 = !consistences.isEmpty() ? consistences.get(consistenceIdx++ % consistences.size()) : defaultBackup;
            MatchVoteEntity r4 = !trends.isEmpty() ? trends.get(trendIdx++ % trends.size()) : defaultBackup;

            boolean isFinalRound = (round == totalMatchRounds);

            if (isFinalRound) {
                r1 = !bodyFits.isEmpty() ? bodyFits.get(bodyFitIdx % bodyFits.size()) : defaultBackup;
                r2 = !colors.isEmpty() ? colors.get(colorIdx % colors.size()) : defaultBackup;
            } else {
                int patternIdx = normalRoundIdx % 3;

                if (patternIdx == 1) {
                    r1 = !bodyFits.isEmpty() ? bodyFits.get(bodyFitIdx++ % bodyFits.size()) : defaultBackup;
                    r2 = !colors.isEmpty() ? colors.get(colorIdx++ % colors.size()) : defaultBackup;
                } else if (patternIdx == 2) {
                    r1 = !vibes.isEmpty() ? vibes.get(vibeIdx++ % vibes.size()) : defaultBackup;
                    r2 = !designs.isEmpty() ? designs.get(designIdx++ % designs.size()) : defaultBackup;
                } else {
                    r1 = !matchables.isEmpty() ? matchables.get(matchableIdx++ % matchables.size()) : defaultBackup;
                    r2 = !moods.isEmpty() ? moods.get(moodIdx++ % moods.size()) : defaultBackup;
                }
                normalRoundIdx++;
            }

            //  가독성과 안전성을 한 번에 잡는 엔티티 리스트 적재
            addCandidateIfNotNull(voteCandidates, matchId, round, r1);
            addCandidateIfNotNull(voteCandidates, matchId, round, r2);
            addCandidateIfNotNull(voteCandidates, matchId, round, r3);
            addCandidateIfNotNull(voteCandidates, matchId, round, r4);
        }

        return new MatchUpCreateResult(matchUps, voteCandidates);
    }

    //  반복되는 null 방어 및 엔티티 생성을 묶어낸 private 내부 메서드
    private void addCandidateIfNotNull(List<MatchVoteCandidateEntity> list, Long matchId, int round, MatchVoteEntity template) {
        if (template == null) return;
        String detail = template.getPreferenceDetail() != null ? template.getPreferenceDetail() : "";
        list.add(new MatchVoteCandidateEntity(
                matchId,
                round,
                template.getId(),
                template.getContent(),
                template.getPreference(),
                detail
        ));
    }

    public List<MatchUpEntity> createNextRoundMatches(Long matchId, int currentRound, List<Long> winnerImageIds) {
        if (winnerImageIds == null || winnerImageIds.isEmpty() || winnerImageIds.size() % 2 != 0) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        int nextRoundNumber = currentRound + 1;
        List<MatchUpEntity> nextMatchUps = new ArrayList<>(winnerImageIds.size() / 2);

        for (int i = 0; i < winnerImageIds.size(); i += 2) {
            nextMatchUps.add(MatchUpEntity.of(
                    new RealMatchUp(matchId, nextRoundNumber, winnerImageIds.get(i), winnerImageIds.get(i + 1))
            ));
        }

        return nextMatchUps;
    }

    private int calculateTargetRound(int n) {
        if (n <= 0) return 0;
        return Integer.highestOneBit(n - 1);
    }
}