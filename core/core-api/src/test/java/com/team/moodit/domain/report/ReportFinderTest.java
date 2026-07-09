package com.team.moodit.domain.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team.moodit.domain.enums.EntityStatus;
import com.team.moodit.domain.enums.PreferenceDetailType;
import com.team.moodit.domain.enums.PreferenceType;
import com.team.moodit.domain.enums.UserMissionState;
import com.team.moodit.storage.db.core.MatchChoiceRepository;
import com.team.moodit.storage.db.core.MatchResultRepository;
import com.team.moodit.storage.db.core.PreferenceDetailVoteCountProjection;
import com.team.moodit.storage.db.core.PreferenceVoteCountProjection;
import com.team.moodit.storage.db.core.UserMissionRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportFinderTest {
    private static final Long USER_ID = 1L;

    @Mock
    private MatchResultRepository matchResultRepository;
    @Mock
    private MatchChoiceRepository matchChoiceRepository;
    @Mock
    private UserMissionRepository userMissionRepository;

    private ReportFinder reportFinder;

    @BeforeEach
    void setUp() {
        reportFinder = new ReportFinder(
                matchResultRepository,
                matchChoiceRepository,
                userMissionRepository
        );

        when(matchResultRepository.countByUserId(USER_ID)).thenReturn(1L);
        when(userMissionRepository.countByUserIdAndStateAndStatus(
                USER_ID,
                UserMissionState.REVIEWED,
                EntityStatus.ACTIVE
        )).thenReturn(0L);
    }

    @Test
    @DisplayName("선호 집계가 없으면 임의 top1 없이 기준 탐색 상태를 반환한다")
    void find_NoPreferenceSelection() {
        when(matchChoiceRepository.countVotedPreferenceByUserId(USER_ID)).thenReturn(List.of());

        PreferenceReport report = reportFinder.find(USER_ID).getPreferenceReport();

        assertThat(report.getResultType()).isEqualTo(PreferenceReportType.NONE);
        assertThat(report.getTopPreference()).isNull();
        assertThat(report.getTopPreferenceDetail()).isNull();
        assertThat(report.getCriteria()).isEmpty();
    }

    @Test
    @DisplayName("선호 top1이 동률이면 임의 top1 없이 상위 3개 선호 비중을 반환한다")
    void find_PreferenceTopTie() {
        when(matchChoiceRepository.countVotedPreferenceByUserId(USER_ID)).thenReturn(List.of(
                preferenceCount(PreferenceType.AESTHETICS, 3L),
                preferenceCount(PreferenceType.FITNESS, 3L),
                preferenceCount(PreferenceType.TREND, 1L)
        ));

        PreferenceReport report = reportFinder.find(USER_ID).getPreferenceReport();

        assertThat(report.getResultType()).isEqualTo(PreferenceReportType.PREFERENCE_TIE);
        assertThat(report.getTopPreference()).isNull();
        assertThat(report.getCriteria())
                .extracting(PreferenceCriterionShare::getType)
                .containsExactly(
                        PreferenceType.AESTHETICS,
                        PreferenceType.FITNESS,
                        PreferenceType.TREND
                );
        assertThat(report.getCriteria())
                .extracting(PreferenceCriterionShare::getPercentage)
                .containsExactly(43, 43, 14);
    }

    @Test
    @DisplayName("선호와 상세선호가 모두 단독 top1이면 상세선호 3개 비중을 반환한다")
    void find_PreferenceDetailTop() {
        when(matchChoiceRepository.countVotedPreferenceByUserId(USER_ID)).thenReturn(List.of(
                preferenceCount(PreferenceType.AESTHETICS, 5L),
                preferenceCount(PreferenceType.FITNESS, 1L),
                preferenceCount(PreferenceType.TREND, 1L)
        ));
        when(matchChoiceRepository.countVotedPreferenceDetailByUserIdAndPreference(
                USER_ID,
                PreferenceType.AESTHETICS.name()
        )).thenReturn(List.of(
                preferenceDetailCount(PreferenceDetailType.DESIGN, 3L),
                preferenceDetailCount(PreferenceDetailType.MOOD, 2L)
        ));

        PreferenceReport report = reportFinder.find(USER_ID).getPreferenceReport();

        assertThat(report.getResultType()).isEqualTo(PreferenceReportType.PREFERENCE_DETAIL);
        assertThat(report.getTopPreference().getType()).isEqualTo(PreferenceType.AESTHETICS);
        assertThat(report.getTopPreferenceDetail().getDetailType()).isEqualTo(PreferenceDetailType.DESIGN);
        assertThat(report.getCriteria())
                .extracting(PreferenceCriterionShare::getDetailType)
                .containsExactly(
                        PreferenceDetailType.DESIGN,
                        PreferenceDetailType.MOOD,
                        PreferenceDetailType.COLOR
                );
        assertThat(report.getCriteria())
                .extracting(PreferenceCriterionShare::getPercentage)
                .containsExactly(60, 40, 0);
    }

    @Test
    @DisplayName("선호 top1은 단독이지만 상세선호가 동률이면 선호 상위 3개 비중으로 내려준다")
    void find_PreferenceDetailTie() {
        when(matchChoiceRepository.countVotedPreferenceByUserId(USER_ID)).thenReturn(List.of(
                preferenceCount(PreferenceType.AESTHETICS, 6L),
                preferenceCount(PreferenceType.FITNESS, 2L),
                preferenceCount(PreferenceType.TREND, 1L)
        ));
        when(matchChoiceRepository.countVotedPreferenceDetailByUserIdAndPreference(
                USER_ID,
                PreferenceType.AESTHETICS.name()
        )).thenReturn(List.of(
                preferenceDetailCount(PreferenceDetailType.DESIGN, 3L),
                preferenceDetailCount(PreferenceDetailType.MOOD, 3L)
        ));

        PreferenceReport report = reportFinder.find(USER_ID).getPreferenceReport();

        assertThat(report.getResultType()).isEqualTo(PreferenceReportType.PREFERENCE_ONLY);
        assertThat(report.getTopPreference().getType()).isEqualTo(PreferenceType.AESTHETICS);
        assertThat(report.getTopPreferenceDetail()).isNull();
        assertThat(report.getCriteria())
                .extracting(PreferenceCriterionShare::getType)
                .containsExactly(
                        PreferenceType.AESTHETICS,
                        PreferenceType.FITNESS,
                        PreferenceType.TREND
                );
        assertThat(report.getCriteria())
                .extracting(PreferenceCriterionShare::getPercentage)
                .containsExactly(67, 22, 11);
    }

    @Test
    @DisplayName("선호가 하나만 집계되어도 0% 항목을 포함해 그래프 항목 3개를 반환한다")
    void find_SinglePreferenceStillReturnsThreeDistributions() {
        when(matchChoiceRepository.countVotedPreferenceByUserId(USER_ID)).thenReturn(List.of(
                preferenceCount(PreferenceType.CONSISTENCE, 4L)
        ));

        PreferenceReport report = reportFinder.find(USER_ID).getPreferenceReport();

        assertThat(report.getResultType()).isEqualTo(PreferenceReportType.PREFERENCE_ONLY);
        assertThat(report.getTopPreference().getType()).isEqualTo(PreferenceType.CONSISTENCE);
        assertThat(report.getCriteria())
                .extracting(PreferenceCriterionShare::getType)
                .containsExactly(
                        PreferenceType.CONSISTENCE,
                        PreferenceType.AESTHETICS,
                        PreferenceType.FITNESS
                );
        assertThat(report.getCriteria())
                .extracting(PreferenceCriterionShare::getPercentage)
                .containsExactly(100, 0, 0);
    }

    private PreferenceVoteCountProjection preferenceCount(
            PreferenceType preferenceType,
            Long count
    ) {
        return new TestPreferenceVoteCount(preferenceType.name(), count);
    }

    private PreferenceDetailVoteCountProjection preferenceDetailCount(
            PreferenceDetailType preferenceDetailType,
            Long count
    ) {
        return new TestPreferenceDetailVoteCount(preferenceDetailType.name(), count);
    }

    private record TestPreferenceVoteCount(
            String preference,
            Long count
    ) implements PreferenceVoteCountProjection {
        @Override
        public String getPreference() {
            return preference;
        }

        @Override
        public Long getCount() {
            return count;
        }
    }

    private record TestPreferenceDetailVoteCount(
            String preferenceDetail,
            Long count
    ) implements PreferenceDetailVoteCountProjection {
        @Override
        public String getPreferenceDetail() {
            return preferenceDetail;
        }

        @Override
        public Long getCount() {
            return count;
        }
    }
}
