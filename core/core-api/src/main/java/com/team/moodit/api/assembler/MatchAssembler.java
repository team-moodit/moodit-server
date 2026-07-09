package com.team.moodit.api.assembler;

import com.team.moodit.api.controller.v1.response.MatchResponse;
import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.domain.match.Match;
import com.team.moodit.domain.match.MatchFinder;
import com.team.moodit.domain.match.MatchImage;
import com.team.moodit.domain.match.MatchImageReader;
import com.team.moodit.domain.match.MatchService;
import com.team.moodit.support.OffsetLimit;
import com.team.moodit.support.Page;
import com.team.moodit.support.auth.ApiUser;
import com.team.moodit.support.file.File;
import com.team.moodit.support.file.FileReader;
import com.team.moodit.support.response.PageResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchAssembler {
    private final MatchFinder matchFinder;
    private final MatchImageReader matchImageReader;
    private final FileReader fileReader;

    public PageResponse<MatchResponse> getMatches(ApiUser apiUser, MatchState state, OffsetLimit offsetLimit) {
        Page<Match> result = matchFinder.find(apiUser.getId(), state, offsetLimit);

        List<MatchImage> matchImages = matchImageReader.getMatchImages(result.content().stream().map(Match::getRepresentativeImageId).toList());

        List<File> files = fileReader.getFiles(matchImages.stream().map(MatchImage::getFileId).toList());
        Map<Long, File> fileMap = files.stream().collect(Collectors.toMap(File::getId, it -> it));
        Map<Long, File> matchImageFileMap = matchImages.stream().collect(Collectors.toMap(
                MatchImage::getId,
                matchImage -> fileMap.get(matchImage.getFileId())
        ));

        return new PageResponse<>(MatchResponse.of(result.content(), matchImageFileMap), result.totalCount(), result.hasNext());
    }
}
