package com.team.moodit.domain.match;

import com.team.moodit.storage.db.core.MatchUpEntity;
import com.team.moodit.storage.db.core.MatchUpRepository;
import com.team.moodit.storage.db.core.ChoiceReasonEntity;
import com.team.moodit.storage.db.core.ChoiceReasonRepository;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import com.team.moodit.support.file.FileReader;
import com.team.moodit.support.file.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component // 📌 팀원 컨벤션 반영: Logic Layer 빈 등록
@RequiredArgsConstructor
public class MatchReader {

    private final MatchUpRepository matchUpRepository;
    private final ChoiceReasonRepository choiceReasonRepository; // choice_reason 테이블 연동
    private final FileReader fileReader; // 팀원이 만들어둔 파일 리더

    public MatchStart read(Long matchId) {
        // 1. 현재 투표해야 하는 첫 번째 대진(NEED_VOTE) 엔티티 조회 (팀원 공통 에러 양식 적용)
        MatchUpEntity entity = matchUpRepository.findById(matchId)
                .orElseThrow(() -> new ApiException(ErrorType.NOT_FOUND));

        // 2. [팀원 인프라 연동] FileReader를 거쳐 호스트 주소가 조립된 완벽한 파일 객체 획득
        File fileA = fileReader.getFile(entity.getCandidateAId());
        File fileB = fileReader.getFile(entity.getCandidateBId());

        // 3. [기획서 명세 구현] choice_reason 테이블 전체 목록을 퍼 올려 무작위 4개 추출 후 셔플
        List<ChoiceReasonEntity> allReasons = choiceReasonRepository.findAll();
        Collections.shuffle(allReasons);
        List<MatchReason> sampledReasons = allReasons.stream()
                .limit(4) // 라운드당 4개 배치 제한
                .map(reason -> MatchReason.of(reason.getId(), reason.getContent()))
                .collect(Collectors.toList());

        // 4. [9장 예선전 처리 핵심 연산] 총 이미지 개수를 구한 뒤 수학적 올림 공식 가동
        // (주의: 레포지토리에 countByMatchId 공통 명세가 없다면 본인 테이블 컬럼명에 맞게 호출하세요!)
        int totalImages = matchUpRepository.countByMatchId(entity.getMatchId());

        // log2(totalImages) 올림 계산 -> 9장일 때 정확히 4라운드가 도출됨
        int totalRounds = (int) Math.ceil(Math.log(totalImages) / Math.log(2));

        // 엔티티에 세팅되어 저장되어 올라온 현재 라운드 번호 그대로 바인딩
        int currentRound = entity.getRoundNumber();

        // 강수 네이밍 결정 규칙 적용
        String roundName = totalImages + "강전";
        if (totalImages == 2) {
            roundName = "결승전";
        } else if (totalImages == 4) {
            roundName = "준결승전";
        }

        // 5. [팀원 DDD 룰 적용] 도메인의 정적 팩토리를 쓰지 않고 순수 생성자로 직접 조립하여 반환
        return new MatchStart(
                entity.getId(),
                "오늘의 무드 찾기 토너먼트", // 임시 타이틀 혹은 매치 엔티티 조인값
                entity.getState(),
                fileA.getId(), fileA.getUrl(), // 완벽하게 접근 가능한 photoUri 주소 주입
                fileB.getId(), fileB.getUrl(),
                totalRounds,
                currentRound,
                roundName,
                sampledReasons
        );
    }
}