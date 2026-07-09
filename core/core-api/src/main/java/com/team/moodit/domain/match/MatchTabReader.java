package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchResumeType;
import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchResultEntity;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.UserMissionEntity;
import com.team.moodit.storage.db.core.UserMissionRepository;
import com.team.moodit.support.Page;
import com.team.moodit.support.file.FileReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MatchTabReader {

    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchImageRepository matchImageRepository;
    private final UserMissionRepository userMissionRepository;
    private final FileReader fileReader;

    @Transactional(readOnly = true)
    public InProgressMatches getInProgressMatches(Long userId, int page, int size) {
        List<MatchEntity> matches = matchRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (matches == null || matches.isEmpty()) {
            return new InProgressMatches(List.of(), 0, false);
        }

        List<Long> matchIds = matches.stream()
                .map(MatchEntity::getId)
                .toList();

        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchIdIn(matchIds);

        if (matchUps == null) {
            matchUps = List.of();
        }

        Map<Long, List<MatchUpEntity>> matchUpMap = matchUps.stream()
                .collect(Collectors.groupingBy(MatchUpEntity::getMatchId));

        List<MatchResultEntity> results =
                matchResultRepository.findByUserIdOrderByCompletedAtDesc(userId);

        if (results == null) {
            results = List.of();
        }

        Map<Long, MatchResultEntity> matchResultMap = results.stream()
                .collect(Collectors.toMap(
                        MatchResultEntity::getMatchId,
                        result -> result,
                        (first, second) -> first
                ));

        List<InProgressMatch> allInProgressMatches = matches.stream()
                .filter(match -> match.getState() == MatchState.ING)
                .map(match -> toInProgressMatch(
                        match,
                        matchUpMap.getOrDefault(match.getId(), Collections.emptyList()),
                        matchResultMap.get(match.getId())
                ))
                .toList();

        Page<InProgressMatch> resultPage = createPage(allInProgressMatches, page, size);

        return new InProgressMatches(
                resultPage.content(),
                resultPage.totalCount(),
                resultPage.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public CompletedMatches getCompletedMatches(Long userId, int page, int size) {
        List<MatchEntity> matches = matchRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (matches == null || matches.isEmpty()) {
            return new CompletedMatches(List.of(), 0, false);
        }

        Map<Long, MatchEntity> matchMap = matches.stream()
                .collect(Collectors.toMap(
                        MatchEntity::getId,
                        match -> match
                ));

        List<UserMissionEntity> userMissions = userMissionRepository.findByUserId(userId);

        if (userMissions == null || userMissions.isEmpty()) {
            return new CompletedMatches(List.of(), 0, false);
        }

        Map<Long, UserMissionEntity> userMissionMap = userMissions.stream()
                .collect(Collectors.toMap(
                        UserMissionEntity::getMatchId,
                        mission -> mission,
                        (first, second) -> first
                ));

        Set<Long> missionSelectedMatchIds = userMissionMap.keySet();

        List<MatchResultEntity> results =
                matchResultRepository.findByUserIdOrderByCompletedAtDesc(userId);

        if (results == null || results.isEmpty()) {
            return new CompletedMatches(List.of(), 0, false);
        }

        List<CompletedMatch> allCompletedMatches = results.stream()
                .filter(result -> matchMap.containsKey(result.getMatchId()))
                .filter(result -> matchMap.get(result.getMatchId()).getState() == MatchState.DONE)
                .filter(result -> missionSelectedMatchIds.contains(result.getMatchId()))
                .map(result -> toCompletedMatch(
                        matchMap.get(result.getMatchId()),
                        result,
                        userMissionMap.get(result.getMatchId())
                ))
                .toList();

        Page<CompletedMatch> resultPage = createPage(allCompletedMatches, page, size);

        return new CompletedMatches(
                resultPage.content(),
                resultPage.totalCount(),
                resultPage.hasNext()
        );
    }

    private InProgressMatch toInProgressMatch(
            MatchEntity match,
            List<MatchUpEntity> matchUps,
            MatchResultEntity matchResult
    ) {
        int totalRound = calculateTotalRound(match.getInitialImageCount());
        int currentRound = calculateCurrentRound(
                match.getInitialImageCount(),
                totalRound,
                matchUps
        );

        MatchResumeType resumeType = MatchResumeType.MATCH_PROGRESS;

        return new InProgressMatch(
                match.getId(),
                matchResult == null ? null : matchResult.getId(),
                match.getTitle(),
                currentRound,
                totalRound,
                match.getUpdatedAt(),
                resumeType
        );
    }

    private CompletedMatch toCompletedMatch(
            MatchEntity match,
            MatchResultEntity result,
            UserMissionEntity userMission
    ) {
        Long winnerImageId = result.getRepresentativeMatchImageId();
        String winnerImageUri = null;

        try {
            if (winnerImageId != null) {
                MatchImageEntity matchImage = matchImageRepository.findById(winnerImageId)
                        .orElse(null);

                if (matchImage != null) {
                    winnerImageUri = fileReader.getFile(matchImage.getFileId()).getUrl();
                }
            }
        } catch (Exception e) {
            winnerImageUri = null;
        }

        LocalDate completedAt = result.getCompletedAt() == null
                ? null
                : result.getCompletedAt().toLocalDate();

        return new CompletedMatch(
                userMission == null ? null : userMission.getId(),
                match.getId(),
                match.getTitle(),
                winnerImageId,
                winnerImageUri,
                completedAt
        );
    }

    private <T> Page<T> createPage(List<T> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        int fromIndex = safePage * safeSize;

        if (fromIndex >= items.size()) {
            return new Page<>(
                    List.of(),
                    items.size(),
                    false
            );
        }

        int toIndex = Math.min(fromIndex + safeSize, items.size());

        return new Page<>(
                items.subList(fromIndex, toIndex),
                items.size(),
                toIndex < items.size()
        );
    }

    private int calculateTotalRound(int initialImageCount) {
        if (initialImageCount <= 0) {
            return 0;
        }

        return Integer.highestOneBit(initialImageCount);
    }

    private int calculateCurrentRound(
            int initialImageCount,
            int totalRound,
            List<MatchUpEntity> matchUps
    ) {
        if (totalRound <= 0) {
            return 0;
        }

        if (matchUps == null || matchUps.isEmpty()) {
            return totalRound;
        }

        int currentRoundNumber = matchUps.stream()
                .filter(matchUp -> matchUp.getState() == MatchUpState.NEED_VOTE)
                .map(MatchUpEntity::getRoundNumber)
                .filter(roundNumber -> roundNumber > 0)
                .min(Integer::compareTo)
                .orElse(1);

        currentRoundNumber = Math.max(currentRoundNumber, 1);

        return calculateDisplayRound(
                initialImageCount,
                totalRound,
                currentRoundNumber
        );
    }

    private int calculateDisplayRound(
            int initialImageCount,
            int totalRound,
            int currentRoundNumber
    ) {
        if (totalRound <= 0) {
            return 0;
        }

        int safeRoundNumber = Math.max(currentRoundNumber, 1);

        boolean hasPreliminaryRound = initialImageCount != totalRound;

        if (!hasPreliminaryRound) {
            int divisor = 1 << (safeRoundNumber - 1);
            return totalRound / Math.max(divisor, 1);
        }

        if (safeRoundNumber == 1) {
            return totalRound;
        }

        int divisor = 1 << (safeRoundNumber - 2);
        return totalRound / Math.max(divisor, 1);
    }
}