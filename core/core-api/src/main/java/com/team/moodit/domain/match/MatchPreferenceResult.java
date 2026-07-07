package com.team.moodit.domain.match;

import com.team.moodit.domain.PreferenceDetailTypeScore;
import com.team.moodit.domain.PreferenceTypeScore;
import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.domain.enums.PreferenceType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchPreferenceResult {
    private PreferenceResultType resultType;
    private PreferenceType preferenceType;
    private PreferenceDetailType preferenceDetailType;
    private List<PreferenceTypeScore> preferenceTypeScores;
    private List<PreferenceDetailTypeScore> preferenceDetailTypeScores;

    public List<PreferenceType> getTopRankPreferenceType() {
        return preferenceTypeScores.stream()
                .filter(PreferenceTypeScore::isTopRank)
                .map(PreferenceTypeScore::getPreferenceType)
                .toList();
    }

    public List<PreferenceDetailType> getTopRankPreferenceDetailTypes(PreferenceType preferenceType) {
        return preferenceDetailTypeScores.stream()
                .filter(score -> score.getPreferenceType() == preferenceType)
                .filter(PreferenceDetailTypeScore::isTopRank)
                .map(PreferenceDetailTypeScore::getPreferenceDetailType)
                .toList();
    }
}
