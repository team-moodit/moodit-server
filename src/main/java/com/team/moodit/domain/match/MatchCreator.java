package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.*;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MatchCreator {

    private final MatchRepository matchRepository;
    private final MatchImageRepository matchImageRepository;
    private final FileRepository fileRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchUpCreator matchUpCreator;
    private final MatchVoteRepository matchVoteRepository;
    private final MatchVoteCandidateRepository matchVoteCandidateRepository;

    @Transactional
    public Long create(Long userId, NewMatch newMatch, List<Long> imageIds) {
        MatchEntity savedMatch = matchRepository.save(
                new MatchEntity(
                        userId,
                        newMatch.getTitle(),
                        MatchState.ING,
                        imageIds.size()
                )
        );

        List<FileEntity> uploadedImages = fileRepository.findByUserIdAndIdIn(userId, imageIds);

        System.out.println("uploadedImageIds = " + uploadedImages.stream()
                .map(FileEntity::getId)
                .toList());

        if (imageIds.size() != uploadedImages.size()) throw new ApiException(ErrorType.INVALID_REQUEST);

        List<MatchImageEntity> savedMatchImages = matchImageRepository.saveAll(
                uploadedImages.stream().map(it ->
                        new MatchImageEntity(
                                savedMatch.getId(),
                                it.getId()
                        )
                ).toList()
        );
        List<Long> savedImageIds = savedMatchImages.stream().map(MatchImageEntity::getId).toList();

        // 2. 전체 템플릿 조회 및 대진표/질문 규칙 조립
        List<MatchVoteEntity> allTemplates = matchVoteRepository.findAll();
        MatchUpCreateResult result = matchUpCreator.createMatches(savedMatch.getId(), savedImageIds, allTemplates);

        // 3. 표준 JPA를 이용한 일괄 저장
        matchUpRepository.saveAll(result.getMatchUps());
        matchVoteCandidateRepository.saveAll(result.getVoteCandidates());

        return savedMatch.getId();
    }
}
