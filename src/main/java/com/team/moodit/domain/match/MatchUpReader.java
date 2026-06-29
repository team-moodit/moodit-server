package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchUpState;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import com.team.moodit.storage.db.core.MatchVoteCandidateRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.file.File;
import com.team.moodit.support.file.FileReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MatchUpReader {

    private final MatchRepository matchRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchVoteCandidateRepository matchVoteCandidateRepository;
    private final FileReader fileReader;

    @Transactional(readOnly = true)
    public MatchUpStart getMatchUp(Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        List<MatchUpEntity> matchUps = matchUpRepository.findByMatchId(matchId);
        if (matchUps == null || matchUps.isEmpty()) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        Optional<MatchUpEntity> currentMatchUpOpt = matchUps.stream()
                .filter(m -> m.getState() == MatchUpState.NEED_VOTE)
                .findFirst();

        if (currentMatchUpOpt.isEmpty()) {
            return MatchUpStart.createCompleted(match.getTitle());
        }

        MatchUpEntity matchUp = currentMatchUpOpt.get();

        File fileA = fileReader.getFile(matchUp.getCandidateAId());
        File fileB = fileReader.getFile(matchUp.getCandidateBId());

        if (fileA == null || fileB == null) {
            throw new ApiException(ErrorType.NOT_FOUND);
        }

        long totalCompletedCount = matchUps.stream()
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .filter(m -> m.getState() == MatchUpState.COMPLETED || m.isVoted())
                .count();

        int globalMatchIndex = (int) totalCompletedCount + 1;

        List<MatchVoteCandidateEntity> sampledVotes = matchVoteCandidateRepository
                .findAllByMatchIdAndRoundNumberOrderByIdAsc(matchId, globalMatchIndex);
        if (sampledVotes == null) {
            sampledVotes = List.of();
        }

        int targetRoundNumber = matchUp.getRoundNumber();

        List<MatchUpEntity> actualMatchesInRound = matchUps.stream()
                .filter(m -> m.getRoundNumber() == targetRoundNumber)
                .filter(m -> m.getCandidateBId() != null && m.getCandidateBId() != 0L)
                .toList();

        int totalMatchUpInRound = actualMatchesInRound.size();

        long completedCountInRound = actualMatchesInRound.stream()
                .filter(m -> m.getState() == MatchUpState.COMPLETED || m.isVoted())
                .count();
        int displayMatchIndex = (int) completedCountInRound + 1;


        int totalImages = match.getInitialImageCount();
        boolean isPerfectBracket = (totalImages == 4 || totalImages == 8 || totalImages == 16 || totalImages == 32);

        String roundName;


        if (targetRoundNumber == 1 && !isPerfectBracket) {
            roundName = "예선전";
        } else if (totalMatchUpInRound == 1) {
            roundName = "결승전";
        } else if (totalMatchUpInRound == 2) {
            roundName = "준결승전";
        } else if (totalMatchUpInRound == 4) {
            roundName = "8강전";
        } else if (totalMatchUpInRound == 8) {
            roundName = "16강전";
        } else if (totalMatchUpInRound == 16) {
            roundName = "32강전";
        } else {
            roundName = "본선";
        }

        boolean isTournamentCompleted = false;

        return new MatchUpStart(
                matchUp.getId(),
                match.getTitle(),
                totalMatchUpInRound,
                displayMatchIndex,
                roundName,
                isTournamentCompleted,
                fileA.getId(),
                fileA.getUrl(),
                fileB.getId(),
                fileB.getUrl(),
                sampledVotes
        );
    }
}