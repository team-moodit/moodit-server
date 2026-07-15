package com.team.moodit.domain.match;

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
    public InProgressMatches getInProgressMatches(
            Long userId,
            int page,
            int size
    ) {
        List<MatchEntity> matches =
                matchRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (matches == null || matches.isEmpty()) {
            return new InProgressMatches(
                    List.of(),
                    0,
                    false
            );
        }

        List<Long> matchIds = matches.stream()
                .map(MatchEntity::getId)
                .toList();

        List<MatchUpEntity> matchUps =
                matchUpRepository.findByMatchIdIn(matchIds);

        if (matchUps == null) {
            matchUps = List.of();
        }

        Map<Long, List<MatchUpEntity>> matchUpMap = matchUps.stream()
                .collect(
                        Collectors.groupingBy(
                                MatchUpEntity::getMatchId
                        )
                );

        List<MatchResultEntity> matchResults =
                matchResultRepository
                        .findByUserIdOrderByCompletedAtDesc(userId);

        if (matchResults == null) {
            matchResults = List.of();
        }

        Map<Long, MatchResultEntity> matchResultMap =
                matchResults.stream()
                        .collect(Collectors.toMap(
                                MatchResultEntity::getMatchId,
                                result -> result,
                                (first, second) -> first
                        ));

        List<InProgressMatch> allInProgressMatches =
                matches.stream()
                        .filter(match ->
                                match.getState() == MatchState.ING
                        )
                        .map(match -> toInProgressMatch(
                                match,
                                matchUpMap.getOrDefault(
                                        match.getId(),
                                        Collections.emptyList()
                                ),
                                matchResultMap.get(match.getId())
                        ))
                        .toList();

        Page<InProgressMatch> resultPage =
                createPage(
                        allInProgressMatches,
                        page,
                        size
                );

        return new InProgressMatches(
                resultPage.content(),
                resultPage.totalCount(),
                resultPage.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public CompletedMatches getCompletedMatches(
            Long userId,
            int page,
            int size
    ) {
        List<MatchEntity> matches =
                matchRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (matches == null || matches.isEmpty()) {
            return new CompletedMatches(
                    List.of(),
                    0,
                    false
            );
        }

        Map<Long, MatchEntity> matchMap =
                matches.stream()
                        .collect(Collectors.toMap(
                                MatchEntity::getId,
                                match -> match
                        ));

        /*
         * 미션이 삭제돼도 완료한 무드매치는 유지한다.
         * UserMission이 없으면 userMissionId만 null로 반환한다.
         */
        List<UserMissionEntity> userMissions =
                userMissionRepository.findByUserId(userId);

        if (userMissions == null) {
            userMissions = List.of();
        }

        Map<Long, UserMissionEntity> userMissionMap =
                userMissions.stream()
                        .collect(Collectors.toMap(
                                UserMissionEntity::getMatchId,
                                mission -> mission,
                                (first, second) -> first
                        ));

        List<MatchResultEntity> results =
                matchResultRepository
                        .findByUserIdOrderByCompletedAtDesc(userId);

        if (results == null || results.isEmpty()) {
            return new CompletedMatches(
                    List.of(),
                    0,
                    false
            );
        }

        /*
         * 완료한 무드매치는 MatchState.DONE과
         * MatchResult 존재 여부로 판단한다.
         */
        List<CompletedMatch> allCompletedMatches =
                results.stream()
                        .filter(result ->
                                matchMap.containsKey(
                                        result.getMatchId()
                                )
                        )
                        .filter(result ->
                                matchMap.get(result.getMatchId())
                                        .getState() == MatchState.DONE
                        )
                        .map(result -> toCompletedMatch(
                                matchMap.get(result.getMatchId()),
                                result,
                                userMissionMap.get(
                                        result.getMatchId()
                                )
                        ))
                        .toList();

        Page<CompletedMatch> resultPage =
                createPage(
                        allCompletedMatches,
                        page,
                        size
                );

        return new CompletedMatches(
                resultPage.content(),
                resultPage.totalCount(),
                resultPage.hasNext()
        );
    }

    /**
     * 진행 중인 매치 목록 항목 생성
     *
     * currentMatchProgress:
     * 사용자가 현재 진행해야 하는 실제 투표 경기 순서
     *
     * finalMatchProgress:
     * 예선을 포함하여 우승자를 결정할 때까지 필요한 전체 경기 수
     */
    private InProgressMatch toInProgressMatch(
            MatchEntity match,
            List<MatchUpEntity> matchUps,
            MatchResultEntity matchResult
    ) {
        int initialImageCount =
                match.getInitialImageCount();

        int totalRound =
                calculateTotalRound(initialImageCount);

        int currentRound = calculateCurrentRound(
                initialImageCount,
                totalRound,
                matchUps
        );

        /*
         * 사용자가 실제로 투표 완료한 경기만 집계한다.
         *
         * SKIPPED는 부전승으로 자동 처리된 경기이므로
         * 사용자의 투표 진행 횟수에는 포함하지 않는다.
         */
        long completedMatchCount = matchUps.stream()
                .filter(matchUp ->
                        matchUp.getState()
                                == MatchUpState.COMPLETED
                )
                .count();

        /*
         * 단판 토너먼트에서 우승자를 결정하는 데 필요한
         * 전체 실제 대결 수는 참가 이미지 수 - 1이다.
         *
         * 8장  -> 7경기
         * 10장 -> 예선 2경기 + 본선 7경기 = 9경기
         * 15장 -> 예선 7경기 + 본선 7경기 = 14경기
         * 16장 -> 15경기
         */
        int finalMatchProgress =
                Math.max(initialImageCount - 1, 0);

        /*
         * 현재 진행할 경기 번호
         *
         * 투표 완료 경기 수 + 1
         * 모든 경기를 완료했다면 전체 경기 수를 초과하지 않는다.
         */
        int currentMatchProgress =
                finalMatchProgress == 0
                        ? 0
                        : Math.min(
                        (int) completedMatchCount + 1,
                        finalMatchProgress
                );

        return new InProgressMatch(
                match.getId(),
                matchResult == null
                        ? null
                        : matchResult.getId(),
                match.getState(),
                match.getTitle(),
                currentRound,
                totalRound,
                currentMatchProgress,
                finalMatchProgress,
                match.getUpdatedAt()
        );
    }

    private CompletedMatch toCompletedMatch(
            MatchEntity match,
            MatchResultEntity result,
            UserMissionEntity userMission
    ) {
        Long winnerImageId =
                result.getRepresentativeMatchImageId();

        String winnerImageUri = null;

        try {
            if (winnerImageId != null) {
                MatchImageEntity matchImage =
                        matchImageRepository
                                .findById(winnerImageId)
                                .orElse(null);

                if (matchImage != null) {
                    winnerImageUri =
                            fileReader.getFile(
                                    matchImage.getFileId()
                            ).getUrl();
                }
            }
        } catch (Exception e) {
            winnerImageUri = null;
        }

        LocalDate completedAt =
                result.getCompletedAt() == null
                        ? null
                        : result.getCompletedAt()
                        .toLocalDate();

        return new CompletedMatch(
                userMission == null
                        ? null
                        : userMission.getId(),
                match.getId(),
                match.getTitle(),
                winnerImageId,
                winnerImageUri,
                completedAt
        );
    }

    private <T> Page<T> createPage(
            List<T> items,
            int page,
            int size
    ) {
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

        int toIndex =
                Math.min(
                        fromIndex + safeSize,
                        items.size()
                );

        return new Page<>(
                items.subList(fromIndex, toIndex),
                items.size(),
                toIndex < items.size()
        );
    }

    /**
     * 입력 사진 수를 기준으로 본선 라운드를 계산한다.
     *
     * 8장     -> 8강
     * 9~15장  -> 예선 후 8강
     * 16장    -> 16강
     * 17~31장 -> 예선 후 16강
     * 32장    -> 32강
     */
    private int calculateTotalRound(
            int initialImageCount
    ) {
        if (initialImageCount <= 0) {
            return 0;
        }

        return Integer.highestOneBit(
                initialImageCount
        );
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

        /*
         * 아직 투표하지 않은 대진 중 가장 앞선 라운드를 찾는다.
         */
        int currentRoundNumber = matchUps.stream()
                .filter(matchUp ->
                        matchUp.getState()
                                == MatchUpState.NEED_VOTE
                )
                .map(MatchUpEntity::getRoundNumber)
                .filter(roundNumber ->
                        roundNumber > 0
                )
                .min(Integer::compareTo)
                .orElseGet(() ->
                        matchUps.stream()
                                .map(
                                        MatchUpEntity::getRoundNumber
                                )
                                .filter(roundNumber ->
                                        roundNumber > 0
                                )
                                .max(Integer::compareTo)
                                .orElse(1)
                );

        return calculateDisplayRound(
                initialImageCount,
                totalRound,
                currentRoundNumber
        );
    }

    /**
     * 내부 roundNumber를 사용자에게 표시할 강수로 변환한다.
     *
     * 예선이 없는 경우:
     * roundNumber 1 -> 8강
     * roundNumber 2 -> 4강
     * roundNumber 3 -> 결승
     *
     * 예선이 있는 경우:
     * roundNumber 1 -> 예선 이후 진입할 본선 강수
     * roundNumber 2 -> 본선 시작
     */
    private int calculateDisplayRound(
            int initialImageCount,
            int totalRound,
            int currentRoundNumber
    ) {
        if (totalRound <= 0) {
            return 0;
        }

        int safeRoundNumber =
                Math.max(currentRoundNumber, 1);

        boolean hasPreliminaryRound =
                initialImageCount != totalRound;

        if (!hasPreliminaryRound) {
            int divisor =
                    1 << (safeRoundNumber - 1);

            return Math.max(
                    totalRound / Math.max(divisor, 1),
                    2
            );
        }

        if (safeRoundNumber == 1) {
            return totalRound;
        }

        int divisor =
                1 << (safeRoundNumber - 2);

        return Math.max(
                totalRound / Math.max(divisor, 1),
                2
        );
    }
}