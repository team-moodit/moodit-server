package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchResultEntity;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.support.Page;
import com.team.moodit.support.file.FileReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

        List<MatchResultEntity> results =
                matchResultRepository.findByUserIdOrderByCompletedAtDesc(userId);

        Set<Long> completedMatchIds = results.stream()
                .map(MatchResultEntity::getMatchId)
                .collect(Collectors.toSet());

        List<MatchUpEntity> matchUps =
                matchUpRepository.findByMatchIdIn(matchIds);

        Map<Long, List<MatchUpEntity>> matchUpMap = matchUps.stream()
                .collect(Collectors.groupingBy(MatchUpEntity::getMatchId));

        List<InProgressMatch> allInProgressMatches = matches.stream()
                .filter(match -> !completedMatchIds.contains(match.getId()))
                .map(match -> toInProgressMatch(
                        match,
                        matchUpMap.getOrDefault(match.getId(), Collections.emptyList())
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

        List<MatchResultEntity> results =
                matchResultRepository.findByUserIdOrderByCompletedAtDesc(userId);

        List<CompletedMatch> allCompletedMatches = results.stream()
                .filter(result -> matchMap.containsKey(result.getMatchId()))
                .map(result -> toCompletedMatch(
                        matchMap.get(result.getMatchId()),
                        result
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
            List<MatchUpEntity> matchUps
    ) {
        int totalRound = calculateTotalRound(match.getInitialImageCount());
        int currentRound = calculateCurrentRound(
                match.getInitialImageCount(),
                totalRound,
                matchUps
        );

        return new InProgressMatch(
                match.getId(),
                match.getTitle(),
                currentRound,
                totalRound,
                match.getUpdatedAt()
        );
    }

    private CompletedMatch toCompletedMatch(
            MatchEntity match,
            MatchResultEntity result
    ) {
        String winnerImageUri = fileReader.getFile(
                result.getRepresentativeMatchImageId()
        ).getUrl();

        return new CompletedMatch(
                match.getId(),
                match.getTitle(),
                result.getRepresentativeMatchImageId(),
                winnerImageUri,
                result.getCompletedAt().toLocalDate()
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
        return Integer.highestOneBit(initialImageCount);
    }

    private int calculateCurrentRound(
            int initialImageCount,
            int totalRound,
            List<MatchUpEntity> matchUps
    ) {
        if (matchUps == null || matchUps.isEmpty()) {
            return totalRound;
        }

        int currentRoundNumber = matchUps.stream()
                .filter(matchUp -> matchUp.getState() == MatchUpState.NEED_VOTE)
                .map(MatchUpEntity::getRoundNumber)
                .min(Integer::compareTo)
                .orElse(1);

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
        boolean hasPreliminaryRound = initialImageCount != totalRound;

        if (!hasPreliminaryRound) {
            return totalRound / (int) Math.pow(2, currentRoundNumber - 1);
        }

        if (currentRoundNumber == 1) {
            return totalRound;
        }

        return totalRound / (int) Math.pow(2, currentRoundNumber - 2);
    }
}