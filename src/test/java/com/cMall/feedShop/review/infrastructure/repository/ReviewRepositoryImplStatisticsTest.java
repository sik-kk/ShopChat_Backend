package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRepositoryImpl 통계 메서드 테스트")
class ReviewRepositoryImplStatisticsTest {

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @InjectMocks
    private ReviewRepositoryImpl reviewRepository;

    @Test
    @DisplayName("Cushion 분포 조회가 주어졌을때_getCushionDistributionByProductId 호출하면_Map으로 변환되어 반환된다")
    void givenCushionDistribution_whenGetCushionDistributionByProductId_thenReturnMap() {
        // given
        Long productId = 1L;
        List<Object[]> queryResults = Arrays.<Object[]>asList(
                new Object[]{Cushion.SOFT, 3L},
                new Object[]{Cushion.MEDIUM, 5L},
                new Object[]{Cushion.FIRM, 2L}
        );
        given(reviewJpaRepository.findCushionDistributionByProductId(productId)).willReturn(queryResults);

        // when
        Map<Cushion, Long> result = reviewRepository.getCushionDistributionByProductId(productId);

        // then
        assertThat(result)
                .hasSize(3)
                .containsEntry(Cushion.SOFT, 3L)
                .containsEntry(Cushion.MEDIUM, 5L)
                .containsEntry(Cushion.FIRM, 2L);
        verify(reviewJpaRepository, times(1)).findCushionDistributionByProductId(productId);
    }

    @Test
    @DisplayName("SizeFit 분포 조회가 주어졌을때_getSizeFitDistributionByProductId 호출하면_Map으로 변환되어 반환된다")
    void givenSizeFitDistribution_whenGetSizeFitDistributionByProductId_thenReturnMap() {
        // given
        Long productId = 1L;
        List<Object[]> queryResults = Arrays.<Object[]>asList(
                new Object[]{SizeFit.SMALL, 2L},
                new Object[]{SizeFit.NORMAL, 6L},
                new Object[]{SizeFit.BIG, 1L}
        );
        given(reviewJpaRepository.findSizeFitDistributionByProductId(productId)).willReturn(queryResults);

        // when
        Map<SizeFit, Long> result = reviewRepository.getSizeFitDistributionByProductId(productId);

        // then
        assertThat(result)
                .hasSize(3)
                .containsEntry(SizeFit.SMALL, 2L)
                .containsEntry(SizeFit.NORMAL, 6L)
                .containsEntry(SizeFit.BIG, 1L);
        verify(reviewJpaRepository, times(1)).findSizeFitDistributionByProductId(productId);
    }

    @Test
    @DisplayName("Stability 분포 조회가 주어졌을때_getStabilityDistributionByProductId 호출하면_Map으로 변환되어 반환된다")
    void givenStabilityDistribution_whenGetStabilityDistributionByProductId_thenReturnMap() {
        // given
        Long productId = 1L;
        List<Object[]> queryResults = Arrays.<Object[]>asList(
                new Object[]{Stability.NORMAL, 1L},
                new Object[]{Stability.STABLE, 4L},
                new Object[]{Stability.VERY_STABLE, 3L}
        );
        given(reviewJpaRepository.findStabilityDistributionByProductId(productId)).willReturn(queryResults);

        // when
        Map<Stability, Long> result = reviewRepository.getStabilityDistributionByProductId(productId);

        // then
        assertThat(result)
                .hasSize(3)
                .containsEntry(Stability.NORMAL, 1L)
                .containsEntry(Stability.STABLE, 4L)
                .containsEntry(Stability.VERY_STABLE, 3L);
        verify(reviewJpaRepository, times(1)).findStabilityDistributionByProductId(productId);
    }

    @Test
    @DisplayName("빈 결과가 주어졌을때_통계 분포 조회하면_빈 Map이 반환된다")
    void givenEmptyResults_whenGetDistribution_thenReturnEmptyMap() {
        // given
        Long productId = 999L;
        given(reviewJpaRepository.findCushionDistributionByProductId(productId)).willReturn(Collections.emptyList());

        // when
        Map<Cushion, Long> result = reviewRepository.getCushionDistributionByProductId(productId);

        // then
        assertThat(result).isEmpty();
        verify(reviewJpaRepository, times(1)).findCushionDistributionByProductId(productId);
    }

    @Test
    @DisplayName("여러개 Cushion 옵션이 주어졌을때_getCushionDistributionByProductId 호출하면_모든 옵션이 포함된 Map이 반환된다")
    void givenMultipleCushionOptions_whenGetCushionDistributionByProductId_thenReturnCompleteMap() {
        // given
        Long productId = 1L;
        List<Object[]> queryResults = Arrays.<Object[]>asList(
                new Object[]{Cushion.VERY_SOFT, 1L},
                new Object[]{Cushion.SOFT, 2L},
                new Object[]{Cushion.MEDIUM, 4L},
                new Object[]{Cushion.FIRM, 2L},
                new Object[]{Cushion.VERY_FIRM, 1L}
        );
        given(reviewJpaRepository.findCushionDistributionByProductId(productId)).willReturn(queryResults);

        // when
        Map<Cushion, Long> result = reviewRepository.getCushionDistributionByProductId(productId);

        // then
        assertThat(result)
                .hasSize(5)
                .containsEntry(Cushion.VERY_SOFT, 1L)
                .containsEntry(Cushion.SOFT, 2L)
                .containsEntry(Cushion.MEDIUM, 4L)
                .containsEntry(Cushion.FIRM, 2L)
                .containsEntry(Cushion.VERY_FIRM, 1L);
        verify(reviewJpaRepository, times(1)).findCushionDistributionByProductId(productId);
    }

    @Test
    @DisplayName("여러개 SizeFit 옵션이 주어졌을때_getSizeFitDistributionByProductId 호출하면_모든 옵션이 포함된 Map이 반환된다")
    void givenMultipleSizeFitOptions_whenGetSizeFitDistributionByProductId_thenReturnCompleteMap() {
        // given
        Long productId = 1L;
        List<Object[]> queryResults = Arrays.<Object[]>asList(
                new Object[]{SizeFit.VERY_SMALL, 1L},
                new Object[]{SizeFit.SMALL, 2L},
                new Object[]{SizeFit.NORMAL, 5L},
                new Object[]{SizeFit.BIG, 1L},
                new Object[]{SizeFit.VERY_BIG, 1L}
        );
        given(reviewJpaRepository.findSizeFitDistributionByProductId(productId)).willReturn(queryResults);

        // when
        Map<SizeFit, Long> result = reviewRepository.getSizeFitDistributionByProductId(productId);

        // then
        assertThat(result)
                .hasSize(5)
                .containsEntry(SizeFit.VERY_SMALL, 1L)
                .containsEntry(SizeFit.SMALL, 2L)
                .containsEntry(SizeFit.NORMAL, 5L)
                .containsEntry(SizeFit.BIG, 1L)
                .containsEntry(SizeFit.VERY_BIG, 1L);
        verify(reviewJpaRepository, times(1)).findSizeFitDistributionByProductId(productId);
    }

    @Test
    @DisplayName("여러개 Stability 옵션이 주어졌을때_getStabilityDistributionByProductId 호출하면_모든 옵션이 포함된 Map이 반환된다")
    void givenMultipleStabilityOptions_whenGetStabilityDistributionByProductId_thenReturnCompleteMap() {
        // given
        Long productId = 1L;
        List<Object[]> queryResults = Arrays.<Object[]>asList(
                new Object[]{Stability.VERY_UNSTABLE, 1L},
                new Object[]{Stability.UNSTABLE, 1L},
                new Object[]{Stability.NORMAL, 2L},
                new Object[]{Stability.STABLE, 4L},
                new Object[]{Stability.VERY_STABLE, 2L}
        );
        given(reviewJpaRepository.findStabilityDistributionByProductId(productId)).willReturn(queryResults);

        // when
        Map<Stability, Long> result = reviewRepository.getStabilityDistributionByProductId(productId);

        // then
        assertThat(result)
                .hasSize(5)
                .containsEntry(Stability.VERY_UNSTABLE, 1L)
                .containsEntry(Stability.UNSTABLE, 1L)
                .containsEntry(Stability.NORMAL, 2L)
                .containsEntry(Stability.STABLE, 4L)
                .containsEntry(Stability.VERY_STABLE, 2L);
        verify(reviewJpaRepository, times(1)).findStabilityDistributionByProductId(productId);
    }

    @Test
    @DisplayName("단일 옵션만 선택된 경우_각 분포 조회하면_해당 옵션만 포함된 Map이 반환된다")
    void givenSingleOptionSelected_whenGetDistribution_thenReturnSingleEntryMap() {
        // given
        Long productId = 1L;

        // Cushion에서 MEDIUM만 선택된 경우
        List<Object[]> cushionResults = Arrays.<Object[]>asList(
                new Object[]{Cushion.MEDIUM, 10L}
        );
        given(reviewJpaRepository.findCushionDistributionByProductId(productId)).willReturn(cushionResults);

        // SizeFit에서 NORMAL만 선택된 경우
        List<Object[]> sizeFitResults = Arrays.<Object[]>asList(
                new Object[]{SizeFit.NORMAL, 10L}
        );
        given(reviewJpaRepository.findSizeFitDistributionByProductId(productId)).willReturn(sizeFitResults);

        // Stability에서 STABLE만 선택된 경우
        List<Object[]> stabilityResults = Arrays.<Object[]>asList(
                new Object[]{Stability.STABLE, 10L}
        );
        given(reviewJpaRepository.findStabilityDistributionByProductId(productId)).willReturn(stabilityResults);

        // when
        Map<Cushion, Long> cushionResult = reviewRepository.getCushionDistributionByProductId(productId);
        Map<SizeFit, Long> sizeFitResult = reviewRepository.getSizeFitDistributionByProductId(productId);
        Map<Stability, Long> stabilityResult = reviewRepository.getStabilityDistributionByProductId(productId);

        // then
        assertThat(cushionResult)
                .hasSize(1)
                .containsEntry(Cushion.MEDIUM, 10L);

        assertThat(sizeFitResult)
                .hasSize(1)
                .containsEntry(SizeFit.NORMAL, 10L);

        assertThat(stabilityResult)
                .hasSize(1)
                .containsEntry(Stability.STABLE, 10L);

        verify(reviewJpaRepository, times(1)).findCushionDistributionByProductId(productId);
        verify(reviewJpaRepository, times(1)).findSizeFitDistributionByProductId(productId);
        verify(reviewJpaRepository, times(1)).findStabilityDistributionByProductId(productId);
    }

    @Test
    @DisplayName("높은 개수의 분포가 주어졌을때_분포 조회하면_정확한 개수가 반환된다")
    void givenHighCountDistribution_whenGetDistribution_thenReturnCorrectCounts() {
        // given
        Long productId = 1L;
        List<Object[]> queryResults = Arrays.<Object[]>asList(
                new Object[]{Cushion.SOFT, 1000L},
                new Object[]{Cushion.MEDIUM, 2500L},
                new Object[]{Cushion.FIRM, 1500L}
        );
        given(reviewJpaRepository.findCushionDistributionByProductId(productId)).willReturn(queryResults);

        // when
        Map<Cushion, Long> result = reviewRepository.getCushionDistributionByProductId(productId);

        // then
        assertThat(result)
                .hasSize(3)
                .containsEntry(Cushion.SOFT, 1000L)
                .containsEntry(Cushion.MEDIUM, 2500L)
                .containsEntry(Cushion.FIRM, 1500L);

        // 총합 검증
        Long totalCount = result.values().stream().mapToLong(Long::longValue).sum();
        assertThat(totalCount).isEqualTo(5000L);

        verify(reviewJpaRepository, times(1)).findCushionDistributionByProductId(productId);
    }
}