package com.team.moodit.domain.missionOffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.team.moodit.domain.enums.MissionOfferState;
import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceResultType;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.storage.db.core.MatchPreferenceResultEntity;
import com.team.moodit.storage.db.core.MatchPreferenceResultRepository;
import com.team.moodit.storage.db.core.MatchResultEntity;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.storage.db.core.MissionOfferCandidateEntity;
import com.team.moodit.storage.db.core.MissionOfferCandidateRepository;
import com.team.moodit.storage.db.core.MissionOfferEntity;
import com.team.moodit.storage.db.core.MissionOfferRepository;
import com.team.moodit.storage.db.core.MissionTemplateEntity;
import com.team.moodit.storage.db.core.MissionTemplateRepository;
import com.team.moodit.storage.db.core.UserMissionRepository;
import com.team.moodit.support.auth.ApiUser;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class MissionOfferServiceTest {
    private final MissionOfferService missionOfferService;
    private final MatchResultRepository matchResultRepository;
    private final MatchPreferenceResultRepository matchPreferenceResultRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final MissionOfferRepository missionOfferRepository;
    private final MissionOfferCandidateRepository missionOfferCandidateRepository;
    private final UserMissionRepository userMissionRepository;

    private MissionTemplateEntity fitnessBodyFitTemplate;
    private MissionTemplateEntity fitnessVibeTemplate;
    private MissionTemplateEntity fitnessMatchableTemplate;
    private MissionTemplateEntity aestheticsColorTemplate;
    private MissionTemplateEntity aestheticsDesignTemplate;
    private MissionTemplateEntity aestheticsMoodTemplate;
    private MissionTemplateEntity consistenceTemplate;
    private MissionTemplateEntity trendTemplate;

    @BeforeAll
    void beforeAll() {
        saveMissionTemplates();
    }

    @BeforeEach
    void beforeEach() {
        userMissionRepository.deleteAll();
    }

    @Test
    @Transactional
    void 매치_선호_결과가_TYPE_AND_DETAIL_라면_상세_선호_타입의_유저_미션을_생성한다() {
        // given
        Long userId = 2L;
        Long matchId = 100L;

        // MatchResult
        MatchResultEntity matchResult = matchResultRepository.save(new MatchResultEntity(
                matchId,
                userId,
                "소장한 아이템과 잘 어울리는지 매치해보기",
                1L,
                16,
                LocalDateTime.of(2026, 6, 23, 12, 0),
                PreferenceResultType.TYPE_AND_DETAIL,
                PreferenceType.FITNESS,
                PreferenceDetailType.MATCHABLE
        ));
        matchPreferenceResultRepository.saveAll(List.of(
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.FITNESS, 5, 1),
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.AESTHETICS, 1, 3),
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.TREND, 2, 2)
        ));

        // when
        MissionOfferCreateResult offerResult = missionOfferService.getOrCreateOffer(new ApiUser(userId), matchResult.getId());
        MissionOffer offer = offerResult.getMissionOffer();

        // then
        // mission offer saved
        MissionOfferEntity savedOffer = missionOfferRepository.findById(offer.getId()).orElseThrow();
        assertThat(savedOffer.getState()).isEqualTo(MissionOfferState.ACCEPTED);
        assertThat(savedOffer.getAcceptedCandidateId()).isEqualTo(offer.getCandidates().getFirst().getId());
        assertThat(savedOffer.getAcceptedAt()).isNotNull();

        // user mission should have single entry
        assertThat(userMissionRepository.findAll()).singleElement()
                .satisfies(userMission -> {
                    assertThat(userMission.getId()).isEqualTo(offerResult.getAssignedMissionId());
                    assertThat(userMission.getUserId()).isEqualTo(userId);
                    assertThat(userMission.getMatchId()).isEqualTo(matchId);
                    assertThat(userMission.getMissionOfferId()).isEqualTo(offer.getId());
                    assertThat(userMission.getMissionTemplateId()).isEqualTo(fitnessMatchableTemplate.getId());
                    assertThat(userMission.getTitle()).isEqualTo(fitnessMatchableTemplate.getTitle());
                    assertThat(userMission.getState()).isEqualTo(UserMissionState.IN_PROGRESS);
                    assertThat(userMission.getCompletedAt()).isNull();
                });
    }

    @Test
    @Transactional
    void 매치_선호_결과가_TYPE_ONLY_라면_상세_선호_타입의_선택_대기_미션을_생성한다() {
        // given
        Long userId = 1L;
        Long matchId = 101L;

        // MatchResult
        MatchResultEntity matchResult = matchResultRepository.save(new MatchResultEntity(
                matchId,
                userId,
                "후보와 비슷한 색감으로 하루 코디해보기",
                1L,
                16,
                LocalDateTime.of(2026, 6, 23, 12, 0),
                PreferenceResultType.TYPE_ONLY,
                PreferenceType.AESTHETICS,
                null
        ));
        matchPreferenceResultRepository.saveAll(List.of(
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.AESTHETICS, 8, 1),
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.FITNESS, 6, 2),
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.TREND, 2, 3)
        ));

        // when
        MissionOfferCreateResult offerResult = missionOfferService.getOrCreateOffer(new ApiUser(userId), matchResult.getId());
        MissionOffer offer = offerResult.getMissionOffer();

        // then
        // mission offer saved
        MissionOfferEntity savedOffer = missionOfferRepository.findById(offer.getId()).orElseThrow();
        assertThat(savedOffer.getMatchResultId()).isEqualTo(matchResult.getId());
        assertThat(savedOffer.getUserId()).isEqualTo(userId);
        assertThat(savedOffer.getState()).isEqualTo(MissionOfferState.NEEDS_SELECTION);
        assertThat(savedOffer.getAcceptedCandidateId()).isNull();
        assertThat(savedOffer.getAcceptedAt()).isNull();

        // mission candidate should have 3 entries
        List<MissionOfferCandidateEntity> savedCandidates = missionOfferCandidateRepository.findByOfferId(offer.getId());
        assertThat(savedCandidates)
                .hasSize(3)
                .extracting(
                        MissionOfferCandidateEntity::getOfferId,
                        MissionOfferCandidateEntity::getMissionTemplateId,
                        MissionOfferCandidateEntity::getTitle,
                        MissionOfferCandidateEntity::getDisplayOrder
                )
                .containsExactlyInAnyOrder(
                        tuple(offer.getId(), aestheticsColorTemplate.getId(), aestheticsColorTemplate.getTitle(), aestheticsColorTemplate.getDisplayOrder()),
                        tuple(offer.getId(), aestheticsDesignTemplate.getId(), aestheticsDesignTemplate.getTitle(), aestheticsDesignTemplate.getDisplayOrder()),
                        tuple(offer.getId(), aestheticsMoodTemplate.getId(), aestheticsMoodTemplate.getTitle(), aestheticsMoodTemplate.getDisplayOrder())
                );

        assertThat(userMissionRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void 매치_선호_결과가_TIE_라면_선호_타입의_선택_대기_미션을_생성한다() {
        // given
        Long userId = 1L;
        Long matchId = 102L;

        // MatchResult
        MatchResultEntity matchResult = matchResultRepository.save(new MatchResultEntity(
                matchId,
                userId,
                "후보와 비슷한 색감으로 하루 코디해보기",
                1L,
                16,
                LocalDateTime.of(2026, 6, 23, 12, 0),
                PreferenceResultType.TIE,
                null,
                null
        ));
        matchPreferenceResultRepository.saveAll(List.of(
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.AESTHETICS, 2, 1),
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.FITNESS, 2, 1),
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.TREND, 2, 1),
                new MatchPreferenceResultEntity(matchResult.getId(), PreferenceType.CONSISTENCE, 2, 1)
        ));

        // when
        MissionOfferCreateResult offerResult = missionOfferService.getOrCreateOffer(new ApiUser(userId), matchResult.getId());
        MissionOffer offer = offerResult.getMissionOffer();

        // then
        // mission offer saved
        MissionOfferEntity savedOffer = missionOfferRepository.findById(offer.getId()).orElseThrow();
        assertThat(savedOffer.getMatchResultId()).isEqualTo(matchResult.getId());
        assertThat(savedOffer.getUserId()).isEqualTo(userId);
        assertThat(savedOffer.getState()).isEqualTo(MissionOfferState.NEEDS_SELECTION);
        assertThat(savedOffer.getAcceptedCandidateId()).isNull();
        assertThat(savedOffer.getAcceptedAt()).isNull();

        // mission candidate should have 4 entries
        List<MissionOfferCandidateEntity> savedCandidates = missionOfferCandidateRepository.findByOfferId(offer.getId());
        assertThat(savedCandidates).hasSize(4);
        assertThat(savedCandidates).extracting(MissionOfferCandidateEntity::getOfferId).containsOnly(offer.getId());
        List<MissionTemplateEntity> selectedTemplates = missionTemplateRepository.findAllById(
                savedCandidates.stream().map(MissionOfferCandidateEntity::getMissionTemplateId).toList()
        );
        assertThat(selectedTemplates).extracting(MissionTemplateEntity::getPreferenceType)
                .containsExactlyInAnyOrder(
                        PreferenceType.AESTHETICS,
                        PreferenceType.FITNESS,
                        PreferenceType.TREND,
                        PreferenceType.CONSISTENCE
                );

        assertThat(userMissionRepository.findAll()).isEmpty();
    }

    // 미션 범위 목록이 기준 정보로 FIXED 되어 있음. (필수 초기 정보)
    private void saveMissionTemplates() {
        List<MissionTemplateEntity> missionTemplates = missionTemplateRepository.saveAll(List.of(
                new MissionTemplateEntity(
                        PreferenceType.FITNESS,
                        PreferenceDetailType.BODY_FIT,
                        "내 체형에 어울리는 스타일인지 시도해보기",
                        1
                ),
                new MissionTemplateEntity(
                        PreferenceType.FITNESS,
                        PreferenceDetailType.VIBE,
                        "이 분위기가 추구미에 맞는지 확인해보기",
                        2
                ),
                new MissionTemplateEntity(
                        PreferenceType.FITNESS,
                        PreferenceDetailType.MATCHABLE,
                        "소장한 아이템과 잘 어울리는지 매치해보기",
                        3
                ),
                new MissionTemplateEntity(
                        PreferenceType.AESTHETICS,
                        PreferenceDetailType.COLOR,
                        "이 색감과 비슷하게 코디해보기",
                        4
                ),
                new MissionTemplateEntity(
                        PreferenceType.AESTHETICS,
                        PreferenceDetailType.DESIGN,
                        "이 디자인과 비슷한 아이템 시도해보기",
                        5
                ),
                new MissionTemplateEntity(
                        PreferenceType.AESTHETICS,
                        PreferenceDetailType.MOOD,
                        "내가 원하는 분위기가 맞는지 연출해보기",
                        6
                ),
                new MissionTemplateEntity(
                        PreferenceType.CONSISTENCE,
                        null,
                        "오래도록 좋아할 스타일인지 도전해보기",
                        7
                ),
                new MissionTemplateEntity(
                        PreferenceType.TREND,
                        null,
                        "이 트렌드가 나에게도 어울리는지 시도해보기",
                        8
                )
        ));

        fitnessBodyFitTemplate = missionTemplates.get(0);
        fitnessVibeTemplate = missionTemplates.get(1);
        fitnessMatchableTemplate = missionTemplates.get(2);
        aestheticsColorTemplate = missionTemplates.get(3);
        aestheticsDesignTemplate = missionTemplates.get(4);
        aestheticsMoodTemplate = missionTemplates.get(5);
        consistenceTemplate = missionTemplates.get(6);
        trendTemplate = missionTemplates.get(7);
    }

    @Test
    void 미션_제안_수락_시_유저_미션을_생성한다() {
        // given
        Long userId = 1L;
        Long matchId = 103L;
        MatchResultEntity matchResult = matchResultRepository.save(new MatchResultEntity(
                matchId,
                userId,
                "후보와 비슷한 색감으로 하루 코디해보기",
                1L,
                16,
                LocalDateTime.of(2026, 6, 23, 12, 0),
                PreferenceResultType.TYPE_ONLY,
                PreferenceType.AESTHETICS,
                null
        ));

        MissionOfferEntity offer = missionOfferRepository.save(
                new MissionOfferEntity(
                        matchResult.getId(),
                        userId,
                        MissionOfferState.NEEDS_SELECTION
                )
        );

        List<MissionOfferCandidateEntity> candidates = missionOfferCandidateRepository.saveAll(List.of(
                new MissionOfferCandidateEntity(
                        offer.getId(),
                        1L,
                        "미션 제안 1",
                        1
                ),
                new MissionOfferCandidateEntity(
                        offer.getId(),
                        2L,
                        "미션 제안 2",
                        2
                ),
                new MissionOfferCandidateEntity(
                        offer.getId(),
                        3L,
                        "미션 제안 3",
                        3
                )
        ));

        OfferAcceptAction acceptAction = new OfferAcceptAction(offer.getId(), candidates.getFirst().getId());

        // when
        missionOfferService.acceptOffer(new ApiUser(userId), acceptAction);

        // then
        MissionOfferEntity savedOffer = missionOfferRepository.findById(offer.getId()).orElseThrow();

        assertThat(userMissionRepository.findAll()).singleElement()
                .satisfies(userMission -> assertThat(userMission.getMatchId()).isEqualTo(matchId));
        assertThat(savedOffer.getState()).isEqualTo(MissionOfferState.ACCEPTED);
        assertThat(savedOffer.getAcceptedCandidateId()).isEqualTo(candidates.getFirst().getId());
        assertThat(savedOffer.getAcceptedAt()).isBefore(LocalDateTime.now());
    }
}
