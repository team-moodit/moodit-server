package com.team.moodit.domain.match;

import com.team.moodit.support.auth.ApiUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchCreator matchCreator;
    private final MatchResultReader matchResultReader;
    private final MatchImageReader matchImageReader;
    private final MatchUpReader matchUpReader;
    private final MatchResultFinder matchResultFinder;
    private final MatchRemover matchRemover;
    private final MatchTabReader matchTabReader;
    private final MatchProgressReader matchProgressReader;
    private final MatchCompletedReader matchCompletedReader;

    public Long createMatch(ApiUser apiUser, NewMatch newMatch, List<Long> imageIds) {
        return matchCreator.create(apiUser.getId(), newMatch, imageIds);
    }

    public List<MatchResult> findMatchResults(List<Long> matchIds) {
        return matchResultFinder.find(matchIds);
    }

    public MatchResult getMatchResult(ApiUser apiUser, Long matchId) {
        return matchResultReader.getMatchResult(apiUser.getId(), matchId);
    }

    public List<MatchImage> getMatchImages(List<Long> imageIds) {
        return matchImageReader.getMatchImages(imageIds);
    }

    public MatchImage getMatchImage(Long imageId) {
        return matchImageReader.getMatchImage(imageId);
    }

    public MatchUpStart getMatchup(Long matchId) {
        return matchUpReader.getMatchUp(matchId);
    }

    public void deleteMatch(Long userId, Long matchId) {
        matchRemover.deleteMatch(userId, matchId);
    }

    public MatchTab getMatchTab(Long userId, int inProgressPage, int inProgressSize, int completedPage, int completedSize) {
        return matchTabReader.getMatchTab(
                userId,
                inProgressPage,
                inProgressSize,
                completedPage,
                completedSize
        );
    }

    public MatchProgressResult getMatchProgress(ApiUser apiUser,Long matchId) {
        return matchProgressReader.getMatchProgress(apiUser.getId(),matchId);
    }
    public MatchCompletedResult getMatchCompleted(ApiUser apiUser,Long matchId){
        return matchCompletedReader.getMatchCompleted(apiUser.getId(),matchId);
    }
}
