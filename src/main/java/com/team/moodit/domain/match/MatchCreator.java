package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.FileEntity;
import com.team.moodit.storage.db.core.FileRepository;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchVoteEntity; //  마스터 템플릿 엔티티 임포트
import com.team.moodit.storage.db.core.MatchVoteRepository; //  마스터 템플릿 레포지토리 임포트
import com.team.moodit.storage.db.core.MatchVoteCandidateRepository; //  신규 후보군 레포지토리 임포트
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchCreator {
    private final MatchRepository matchRepository;
    private final MatchImageRepository matchImageRepository;
    private final FileRepository fileRepository;
    private final MatchUpRepository matchUpRepository;
    private final MatchUpCreator matchUpCreator;

    //  질문 템플릿 조회 및 조합 결과를 저장할 2개의 레포지토리 의존성 추가
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
        if (imageIds.size() != uploadedImages.size()) throw new ApiException(ErrorType.INVALID_REQUEST);

        matchImageRepository.saveAll(
                uploadedImages.stream().map(it ->
                        new MatchImageEntity(
                                savedMatch.getId(),
                                it.getId()
                        )
                ).toList()
        );

        // 1. DB에 기본 양식(기본 템플릿)으로 미리 박아둔 질문 목록 전부 SELECT
        List<MatchVoteEntity> allTemplates = matchVoteRepository.findAll();

        // 2. 대진표 생성 및 피그마 규칙 질문 조립 동시 실행
        MatchUpCreateResult result = matchUpCreator.createMatches(savedMatch.getId(), imageIds, allTemplates);

        //  3. 묶음 객체에서 데이터를 롬복 Getter로 각각 꺼내서 일괄 saveAll() 실행!
        matchUpRepository.saveAll(result.getMatchUps());
        matchVoteCandidateRepository.saveAll(result.getVoteCandidates());

        return savedMatch.getId();
    }
}