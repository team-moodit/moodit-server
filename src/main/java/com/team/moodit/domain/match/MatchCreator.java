package com.team.moodit.domain.match;

import com.team.moodit.domain.enums.MatchState;
import com.team.moodit.storage.db.core.FileEntity;
import com.team.moodit.storage.db.core.FileRepository;
import com.team.moodit.storage.db.core.MatchEntity;
import com.team.moodit.storage.db.core.MatchImageRepository;
import com.team.moodit.storage.db.core.MatchRepository;
import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.MatchVoteCandidateEntity;
import com.team.moodit.storage.db.core.MatchVoteCandidateRepository;
import com.team.moodit.storage.db.core.MatchVoteEntity;
import com.team.moodit.storage.db.core.MatchVoteRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

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

    // JDBC 벌크 인서트로 N+1 쿼리를 단일 쿼리로 최적화.
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Long create(Long userId, NewMatch newMatch, List<Long> imageIds) {
        MatchEntity savedMatch = matchRepository.save(new MatchEntity(userId, newMatch.getTitle(), MatchState.ING, imageIds.size()));
        List<FileEntity> uploadedImages = fileRepository.findByUserIdAndIdIn(userId, imageIds);
        if (imageIds.size() != uploadedImages.size()) throw new ApiException(ErrorType.INVALID_REQUEST);


        //  [변경] 이미지 매핑 11개도 단 1번의 쿼리로 묶어서 전송! (20초 지연 소멸)
        bulkInsertMatchImages(savedMatch.getId(), uploadedImages);

        // 1. DB에 기본 양식(기본 템플릿)으로 미리 박아둔 질문 목록 전부 SELECT
        List<MatchVoteEntity> allTemplates = matchVoteRepository.findAll();

        // 2. 대진표 생성 및 피그마 규칙 질문 조립 동시 실행 (기획 의도 100% 반영 시점)
        MatchUpCreateResult result = matchUpCreator.createMatches(savedMatch.getId(), imageIds, allTemplates);

        //  [변경] 기획대로 조립이 완전히 완료된 리스트를 한 바구니에 담아 단 1번씩만 슛!
        bulkInsertMatchUps(result.getMatchUps());
        bulkInsertVoteCandidates(result.getVoteCandidates());

        return savedMatch.getId();
    }

    /**
     * [추가] 이미지 매핑 데이터 일괄 벌크 인서트 (배포 서버 22초 저격용 숨겨진 열쇠)
     */
    private void bulkInsertMatchImages(Long matchId, List<FileEntity> uploadedImages) {
        if (uploadedImages.isEmpty()) return;

        // 본인의 match_image 테이블 실제 컬럼명 구조와 일치해야 합니다 (match_id, file_id)
        String sql = "INSERT INTO match_image (match_id, file_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FileEntity file = uploadedImages.get(i);
                ps.setLong(1, matchId);
                ps.setLong(2, file.getId());
            }

            @Override
            public int getBatchSize() {
                return uploadedImages.size();
            }
        });
    }

    /**
     * 질문 후보군(40개) 일괄 벌크 인서트
     */
    private void bulkInsertVoteCandidates(List<MatchVoteCandidateEntity> candidates) {
        if (candidates.isEmpty()) return;

        String sql = "INSERT INTO match_vote_candidate (match_id, round_number, vote_id, content, preference, preference_detail, created_at) " + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MatchVoteCandidateEntity c = candidates.get(i);
                ps.setLong(1, c.getMatchId());
                ps.setInt(2, c.getRoundNumber());
                ps.setLong(3, c.getVoteId());
                ps.setString(4, c.getContent());
                ps.setString(5, c.getPreference());
                ps.setString(6, c.getPreferenceDetail() != null ? c.getPreferenceDetail() : "");
                ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            }

            @Override
            public int getBatchSize() {
                return candidates.size();
            }
        });
    }

    /**
     * 대진표 데이터 일괄 벌크 인서트 (부전승 SKIPPED 및 winner_id 반영 수정)
     */
    private void bulkInsertMatchUps(List<MatchUpEntity> matchUps) {
        if (matchUps.isEmpty()) return;

        // 1. 쿼리에 winner_id 컬럼을 추가하고, state도 ?로 동적 처리하도록 변경합니다.
        String sql = "INSERT INTO match_up (match_id, round_number, candidateaid, candidatebid, state, winner_id, created_at, updated_at, version) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MatchUpEntity m = matchUps.get(i);
                ps.setLong(1, m.getMatchId());
                ps.setInt(2, m.getRoundNumber());

                ps.setObject(3, m.getCandidateAId());
                ps.setObject(4, m.getCandidateBId());

                // 2. 하드코딩 대신 엔티티의 진짜 상태(NEED_VOTE 또는 SKIPPED)를 주입합니다.
                ps.setString(5, m.getState() != null ? m.getState().name() : "NEED_VOTE");

                // 3. 부전승 엔티티에 세팅된 winnerId를 추출하여 쿼리에 바인딩합니다. (없으면 null 처리)
                if (m.getWinnerId() != null) {
                    ps.setLong(6, m.getWinnerId());
                } else {
                    ps.setNull(6, java.sql.Types.BIGINT);
                }

                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                ps.setTimestamp(7, now);
                ps.setTimestamp(8, now);
            }

            @Override
            public int getBatchSize() {
                return matchUps.size();
            }
        });
    }

}