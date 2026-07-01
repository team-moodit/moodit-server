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

    private static final String EMPTY_DETAIL = "";

    public MatchUpCreateResult createMatches(Long matchId, List<Long> imageIds, List<MatchVoteEntity> allTemplates) {
        validateInputs(imageIds, allTemplates);

        int totalImages = imageIds.size();
        int targetRound = calculateTargetRound(totalImages);
        int preliminaryMatchCount = totalImages - targetRound;
        int preliminaryPlayerCount = preliminaryMatchCount * 2;
        int totalMatchRounds = totalImages - 1;

        List<MatchUpEntity> matchUps = generateMatchUps(matchId, imageIds, preliminaryPlayerCount);

        Map<PreferenceType, Map<String, List<MatchVoteEntity>>> templatePool = new EnumMap<>(PreferenceType.class);
        for (PreferenceType type : PreferenceType.values()) {
            templatePool.put(type, new HashMap<>());
        }

        for (MatchVoteEntity template : allTemplates) {
            if (template.getPreference() == null) {
                continue;
            }

            PreferenceType type = PreferenceType.from(template.getPreference());
            String detail = normalizeDetail(template.getPreferenceDetail());

            templatePool.get(type)
                    .computeIfAbsent(detail, k -> new ArrayList<>())
                    .add(template);
        }

        for (Map<String, List<MatchVoteEntity>> detailMap : templatePool.values()) {
            for (List<MatchVoteEntity> sentences : detailMap.values()) {
                Collections.shuffle(sentences);
            }
        }

        Map<PreferenceType, List<String>> detailSchedule = new EnumMap<>(PreferenceType.class);

        for (PreferenceType type : PreferenceType.values()) {
            Map<String, List<MatchVoteEntity>> detailMap =
                    templatePool.getOrDefault(type, Collections.emptyMap());

            List<String> details = new ArrayList<>(detailMap.keySet());
            List<String> scheduleList = createDetailSchedule(details, totalMatchRounds);

            detailSchedule.put(type, scheduleList);
        }

        List<MatchVoteCandidateEntity> voteCandidates =
                new ArrayList<>(totalMatchRounds * PreferenceType.values().length);

        Map<String, Integer> usageCounters = new HashMap<>();
        List<String> previousPreferenceOrder = null;

        for (int round = 1; round <= totalMatchRounds; round++) {
            List<MatchVoteEntity> roundCandidates = new ArrayList<>();

            for (PreferenceType type : PreferenceType.values()) {
                String targetDetail = detailSchedule.get(type).get(round - 1);

                MatchVoteEntity candidate = pickCandidate(
                        type,
                        targetDetail,
                        templatePool,
                        usageCounters
                );

                roundCandidates.add(candidate);
            }

            List<String> currentPreferenceOrder;

            do {
                Collections.shuffle(roundCandidates);
                currentPreferenceOrder = roundCandidates.stream()
                        .map(MatchVoteEntity::getPreference)
                        .toList();
            } while (previousPreferenceOrder != null
                    && previousPreferenceOrder.equals(currentPreferenceOrder));

            previousPreferenceOrder = currentPreferenceOrder;

            int displayOrder = 1;

            for (MatchVoteEntity candidate : roundCandidates) {
                voteCandidates.add(new MatchVoteCandidateEntity(
                        matchId,
                        round,
                        displayOrder++,
                        candidate.getId(),
                        candidate.getContent(),
                        candidate.getPreference(),
                        normalizeDetail(candidate.getPreferenceDetail())
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

    private List<String> createDetailSchedule(List<String> details, int totalMatchRounds) {
        List<String> schedule = new ArrayList<>(totalMatchRounds);

        if (details.isEmpty()) {
            for (int i = 0; i < totalMatchRounds; i++) {
                schedule.add(EMPTY_DETAIL);
            }
            return schedule;
        }

        if (details.size() == 1) {
            for (int i = 0; i < totalMatchRounds; i++) {
                schedule.add(details.get(0));
            }
            return schedule;
        }

        List<String> shuffledDetails = new ArrayList<>(details);
        Collections.shuffle(shuffledDetails);

        int index = 0;
        String lastDetail = null;

        while (schedule.size() < totalMatchRounds) {
            String currentDetail = shuffledDetails.get(index % shuffledDetails.size());

            if (lastDetail != null && lastDetail.equals(currentDetail)) {
                Collections.shuffle(shuffledDetails);
                index = 0;
                continue;
            }

            schedule.add(currentDetail);
            lastDetail = currentDetail;
            index++;

            if (index % shuffledDetails.size() == 0) {
                Collections.shuffle(shuffledDetails);
            }
        }

        return schedule;
    }

    private MatchVoteEntity pickCandidate(
            PreferenceType type,
            String targetDetail,
            Map<PreferenceType, Map<String, List<MatchVoteEntity>>> templatePool,
            Map<String, Integer> usageCounters
    ) {
        Map<String, List<MatchVoteEntity>> detailMap = templatePool.get(type);

        if (detailMap == null || detailMap.isEmpty()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        List<MatchVoteEntity> sentences = detailMap.get(targetDetail);

        if (sentences == null || sentences.isEmpty()) {
            sentences = new ArrayList<>();

            for (List<MatchVoteEntity> list : detailMap.values()) {
                sentences.addAll(list);
            }

            Collections.shuffle(sentences);
        }

        if (sentences.isEmpty()) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        String counterKey = type.name() + "_" + targetDetail;
        int currentIdx = usageCounters.getOrDefault(counterKey, 0);

        if (currentIdx >= sentences.size()) {
            Collections.shuffle(sentences);
            currentIdx = 0;
        }

        MatchVoteEntity selected = sentences.get(currentIdx);
        usageCounters.put(counterKey, currentIdx + 1);

        return selected;
    }

    private List<MatchUpEntity> generateMatchUps(Long matchId, List<Long> imageIds, int preliminaryPlayerCount) {
        List<Long> shuffledIds = new ArrayList<>(imageIds);
        Collections.shuffle(shuffledIds);

        List<MatchUpEntity> matchUps = new ArrayList<>(imageIds.size());

        if (preliminaryPlayerCount == 0) {
            for (int i = 0; i < shuffledIds.size(); i += 2) {
                matchUps.add(MatchUpEntity.of(
                        new RealMatchUp(matchId, 1, shuffledIds.get(i), shuffledIds.get(i + 1))
                ));
            }
            return matchUps;
        }

        for (int i = 0; i < preliminaryPlayerCount; i += 2) {
            matchUps.add(MatchUpEntity.of(
                    new RealMatchUp(matchId, 1, shuffledIds.get(i), shuffledIds.get(i + 1))
            ));
        }

        for (int i = preliminaryPlayerCount; i < shuffledIds.size(); i++) {
            matchUps.add(MatchUpEntity.of(
                    new AutoPassMatch(matchId, 1, shuffledIds.get(i))
            ));
        }

        return matchUps;
    }

    private void validateInputs(List<Long> imageIds, List<MatchVoteEntity> allTemplates) {
        if (imageIds == null || imageIds.size() < 8 || imageIds.size() > 32) {
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

    private String normalizeDetail(String detail) {
        return detail != null ? detail : EMPTY_DETAIL;
    }
}