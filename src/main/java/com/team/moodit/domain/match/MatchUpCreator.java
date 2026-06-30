package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.storage.db.core.*;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MatchUpCreator {

    public MatchUpCreateResult createMatches(Long matchId, List<Long> imageIds, List<MatchVoteEntity> allTemplates) {
        validateInputs(imageIds, allTemplates);

        int totalImages = imageIds.size();
        int targetRound = calculateTargetRound(totalImages);
        int firstRoundPlayersCount = (totalImages - targetRound) * 2;
        int totalMatchRounds = totalImages - 1;

        List<MatchUpEntity> matchUps = generateMatchUps(matchId, imageIds, firstRoundPlayersCount);

        // 1. [O(N) 최적화] templatePool 초기화 및 단 1번의 루프로 그룹 매핑 완료
        Map<PreferenceType, Map<String, List<MatchVoteEntity>>> templatePool = new EnumMap<>(PreferenceType.class);
        for (PreferenceType type : PreferenceType.values()) {
            templatePool.put(type, new HashMap<>());
        }

        // 스트림 중복 순회 및 내부 from() 연속 호출 문제를 단일 루프로 해결
        for (MatchVoteEntity template : allTemplates) {
            if (template.getPreference() == null) {
                continue;
            }

            PreferenceType type = PreferenceType.from(template.getPreference());
            String detail = template.getPreferenceDetail() != null ? template.getPreferenceDetail() : "";

            templatePool.get(type)
                    .computeIfAbsent(detail, k -> new ArrayList<>())
                    .add(template);
        }

        // 각 상세 그룹 내 문장 셔플
        for (Map<String, List<MatchVoteEntity>> detailMap : templatePool.values()) {
            for (List<MatchVoteEntity> sentences : detailMap.values()) {
                Collections.shuffle(sentences);
            }
        }

        // 2. 전체 라운드 수에 맞춘 타입별 상세 선호 노출 스케줄 세우기
        Map<PreferenceType, List<String>> detailSchedule = new EnumMap<>(PreferenceType.class);

        for (PreferenceType type : PreferenceType.values()) {
            Map<String, List<MatchVoteEntity>> detailMap = templatePool.getOrDefault(type, Collections.emptyMap());
            List<String> details = new ArrayList<>(detailMap.keySet());
            List<String> scheduleList = new ArrayList<>(totalMatchRounds);

            if (!details.isEmpty()) {
                int quotient = totalMatchRounds / details.size();
                int remainder = totalMatchRounds % details.size();

                Collections.shuffle(details);

                for (int i = 0; i < quotient; i++) {
                    scheduleList.addAll(details);
                }

                if (remainder > 0) {
                    List<String> remainderDetails = new ArrayList<>(details);
                    Collections.shuffle(remainderDetails);
                    for (int i = 0; i < remainder; i++) {
                        scheduleList.add(remainderDetails.get(i));
                    }
                }
            } else {
                for (int i = 0; i < totalMatchRounds; i++) {
                    scheduleList.add("");
                }
            }
            detailSchedule.put(type, scheduleList);
        }

        // 3. 수립된 스케줄에 맞춰 라운드별 투표 문장 매핑
        List<MatchVoteCandidateEntity> voteCandidates = new ArrayList<>(totalMatchRounds * 4);
        Map<String, Integer> usageCounters = new HashMap<>();

        for (int round = 1; round <= totalMatchRounds; round++) {
            List<MatchVoteEntity> roundCandidates = new ArrayList<>();

            for (PreferenceType type : PreferenceType.values()) {
                String targetDetail = detailSchedule.get(type).get(round - 1);
                List<MatchVoteEntity> sentences = templatePool.get(type).get(targetDetail);

                if (sentences == null || sentences.isEmpty()) {
                    roundCandidates.add(allTemplates.get(round % allTemplates.size()));
                    continue;
                }

                String counterKey = type.name() + "_" + targetDetail;
                int currentIdx = usageCounters.getOrDefault(counterKey, 0) % sentences.size();

                roundCandidates.add(sentences.get(currentIdx));
                usageCounters.put(counterKey, currentIdx + 1);
            }

            Collections.shuffle(roundCandidates);

            for (MatchVoteEntity candidate : roundCandidates) {
                voteCandidates.add(new MatchVoteCandidateEntity(
                        matchId, round, candidate.getId(), candidate.getContent(),
                        candidate.getPreference(), candidate.getPreferenceDetail() != null ? candidate.getPreferenceDetail() : ""
                ));
            }
        }
        return new MatchUpCreateResult(matchUps, voteCandidates);
    }

    public List<MatchUpEntity> createNextRoundMatches(Long matchId, int currentRound, List<Long> winnerImageIds) {
        if (winnerImageIds == null || winnerImageIds.size() < 2 || winnerImageIds.size() % 2 != 0) {
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

    private List<MatchUpEntity> generateMatchUps(Long matchId, List<Long> imageIds, int firstRoundPlayersCount) {
        List<Long> shuffledIds = new ArrayList<>(imageIds);
        Collections.shuffle(shuffledIds);

        List<MatchUpEntity> matchUps = new ArrayList<>(imageIds.size());
        for (int i = 0; i < firstRoundPlayersCount; i += 2) {
            matchUps.add(MatchUpEntity.of(new RealMatchUp(matchId, 1, shuffledIds.get(i), shuffledIds.get(i + 1))));
        }
        for (int i = firstRoundPlayersCount; i < imageIds.size(); i++) {
            matchUps.add(MatchUpEntity.of(new AutoPassMatch(matchId, 1, shuffledIds.get(i))));
        }
        return matchUps;
    }

    private void validateInputs(List<Long> imageIds, List<MatchVoteEntity> allTemplates) {
        if (imageIds == null || imageIds.size() < 4 || imageIds.size() > 32) {
            throw new ApiException(ErrorType.INVALID_IMAGE_COUNT);
        }
        if (allTemplates == null || allTemplates.isEmpty()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }

    private int calculateTargetRound(int n) {
        boolean isPowerOfTwo = (n > 0) && ((n & (n - 1)) == 0);
        return isPowerOfTwo ? n : Integer.highestOneBit(n - 1);
    }
}