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

        List<MatchUpEntity> matchUps = new ArrayList<>();
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

        List<MatchVoteCandidateEntity> voteCandidates = new ArrayList<>();

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

        if (bodyFits.isEmpty()) bodyFits.addAll(allTemplates);
        if (colors.isEmpty()) colors.addAll(allTemplates);
        if (vibes.isEmpty()) vibes.addAll(allTemplates);
        if (designs.isEmpty()) designs.addAll(allTemplates);
        if (matchables.isEmpty()) matchables.addAll(allTemplates);
        if (moods.isEmpty()) moods.addAll(allTemplates);
        if (consistences.isEmpty()) consistences.addAll(allTemplates);
        if (trends.isEmpty()) trends.addAll(allTemplates);

        Collections.shuffle(bodyFits);     Collections.shuffle(colors);
        Collections.shuffle(vibes);        Collections.shuffle(designs);
        Collections.shuffle(matchables);   Collections.shuffle(moods);
        Collections.shuffle(consistences); Collections.shuffle(trends);

        //  각 카테고리별로 데이터를 순차적으로 소모하기 위한 전용 포인터 인덱스 정의
        int bodyFitIdx = 0;   int colorIdx = 0;
        int vibeIdx = 0;      int designIdx = 0;
        int matchableIdx = 0; int moodIdx = 0;
        int consistenceIdx = 0; int trendIdx = 0;

        int normalRoundIdx = 1;

        for (int round = 1; round <= totalMatchRounds; round++) {
            MatchVoteEntity r1 = null;
            MatchVoteEntity r2 = null;

            //  전용 포인터를 사용하고 사용 후 증가(Idx++) 시키는 방식으로 매핑 정확도 정합성 확보
            MatchVoteEntity r3 = !consistences.isEmpty() ? consistences.get(consistenceIdx++ % consistences.size()) : null;
            MatchVoteEntity r4 = !trends.isEmpty() ? trends.get(trendIdx++ % trends.size()) : null;

            boolean isFinalRound = (round == totalMatchRounds);

            if (isFinalRound) {
                r1 = !bodyFits.isEmpty() ? bodyFits.get(bodyFitIdx % bodyFits.size()) : (!vibes.isEmpty() ? vibes.get(vibeIdx % vibes.size()) : matchables.get(matchableIdx % matchables.size()));
                r2 = !colors.isEmpty() ? colors.get(colorIdx % colors.size()) : (!designs.isEmpty() ? designs.get(designIdx % designs.size()) : moods.get(moodIdx % moods.size()));
            } else {
                int patternIdx = normalRoundIdx % 3;

                if (patternIdx == 1) {
                    r1 = !bodyFits.isEmpty() ? bodyFits.get(bodyFitIdx++ % bodyFits.size()) : null;
                    r2 = !colors.isEmpty() ? colors.get(colorIdx++ % colors.size()) : null;
                } else if (patternIdx == 2) {
                    r1 = !vibes.isEmpty() ? vibes.get(vibeIdx++ % vibes.size()) : null;
                    r2 = !designs.isEmpty() ? designs.get(designIdx++ % designs.size()) : null;
                } else {
                    r1 = !matchables.isEmpty() ? matchables.get(matchableIdx++ % matchables.size()) : null;
                    r2 = !moods.isEmpty() ? moods.get(moodIdx++ % moods.size()) : null;
                }

                normalRoundIdx++;
            }

            if (r1 != null) voteCandidates.add(new MatchVoteCandidateEntity(matchId, round, r1.getId(), r1.getContent(), r1.getPreference(), r1.getPreferenceDetail()));
            if (r2 != null) voteCandidates.add(new MatchVoteCandidateEntity(matchId, round, r2.getId(), r2.getContent(), r2.getPreference(), r2.getPreferenceDetail()));
            if (r3 != null) voteCandidates.add(new MatchVoteCandidateEntity(matchId, round, r3.getId(), r3.getContent(), r3.getPreference(), r3.getPreferenceDetail()));
            if (r4 != null) voteCandidates.add(new MatchVoteCandidateEntity(matchId, round, r4.getId(), r4.getContent(), r4.getPreference(), r4.getPreferenceDetail()));
        }

        return new MatchUpCreateResult(matchUps, voteCandidates);
    }

    public List<MatchUpEntity> createNextRoundMatches(Long matchId, int currentRound, List<Long> winnerImageIds) {
        if (winnerImageIds == null || winnerImageIds.isEmpty() || winnerImageIds.size() % 2 != 0) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        int nextRoundNumber = currentRound + 1;
        List<MatchUpEntity> nextMatchUps = new ArrayList<>();

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