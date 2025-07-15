package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.review.application.ReviewService;
import com.cMall.feedShop.review.application.ReviewStatisticsService;
import com.cMall.feedShop.review.application.dto.response.ProductReviewSummaryResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewStatisticsResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@WebMvcTest(ReviewProductController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReviewProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private ReviewStatisticsService reviewStatisticsService;

    @Test
    @DisplayName("Given product id_When get product review summary_Then return 200 ok")
    void givenProductId_whenGetProductReviewSummary_thenReturn200Ok() throws Exception {
        // given
        Long productId = 1L;

        // 최근 리뷰 목록 생성
        List<ReviewSummaryResponse> recentReviews = List.of(
                ReviewSummaryResponse.builder()
                        .reviewId(1L)
                        .userId(1L)
                        .productId(productId)
                        .reviewTitle("정말 좋아요!")
                        .content("최고예요!")
                        .rating(5)
                        .createdAt(LocalDateTime.now())
                        .images(new ArrayList<>())
                        .build(),
                ReviewSummaryResponse.builder()
                        .reviewId(2L)
                        .userId(2L)
                        .productId(productId)
                        .reviewTitle("편해요")
                        .content("편해요")
                        .rating(4)
                        .createdAt(LocalDateTime.now())
                        .images(new ArrayList<>())
                        .build()
        );

        // 평점 분포 생성
        ProductReviewSummaryResponse.RatingDistribution ratingDistribution =
                ProductReviewSummaryResponse.RatingDistribution.builder()
                        .fiveStar(15L)
                        .fourStar(8L)
                        .threeStar(2L)
                        .twoStar(0L)
                        .oneStar(0L)
                        .build();

        ProductReviewSummaryResponse response = ProductReviewSummaryResponse.builder()
                .productId(productId)
                .averageRating(4.5)
                .totalReviews(25L)
                .ratingDistribution(ratingDistribution)
                .mostCommonSizeFit("PERFECT")
                .recentReviews(recentReviews)
                .build();

        when(reviewStatisticsService.getProductReviewSummary(productId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/{productId}/reviews/summary", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(25))
                .andExpect(jsonPath("$.mostCommonSizeFit").value("PERFECT"))
                .andExpect(jsonPath("$.recentReviews.length()").value(2));

        verify(reviewStatisticsService, times(1)).getProductReviewSummary(productId);
    }

    @Test
    @DisplayName("Given product id_When get review statistics_Then return 200 ok")
    void givenProductId_whenGetReviewStatistics_thenReturn200Ok() throws Exception {
        // given
        Long productId = 1L;
        ReviewStatisticsResponse response = ReviewStatisticsResponse.builder()
                .productId(productId)
                .averageRating(4.2)
                .totalReviews(50L)
                .ratingDistribution(Map.of(5, 20L, 4, 15L, 3, 10L, 2, 3L, 1, 2L))
                .sizeFitDistribution(Map.of("PERFECT", 30L, "BIG", 15L, "SMALL", 5L))
                .stabilityDistribution(Map.of("VERY_STABLE", 25L, "STABLE", 20L, "NORMAL", 5L))
                .build();

        when(reviewStatisticsService.getProductStatistics(productId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/{productId}/reviews/statistics", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.averageRating").value(4.2))
                .andExpect(jsonPath("$.totalReviews").value(50))
                .andExpect(jsonPath("$.ratingDistribution.5").value(20))
                .andExpect(jsonPath("$.sizeFitDistribution.PERFECT").value(30))
                .andExpect(jsonPath("$.stabilityDistribution.VERY_STABLE").value(25));

        verify(reviewStatisticsService, times(1)).getProductStatistics(productId);
    }

    @Test
    @DisplayName("Given invalid product id_When get product summary_Then return 404")
    void givenInvalidProductId_whenGetProductSummary_thenReturn404() throws Exception {
        // given
        Long invalidProductId = 999L;

        when(reviewStatisticsService.getProductReviewSummary(invalidProductId))
                .thenThrow(new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/products/{productId}/reviews/summary", invalidProductId))
                .andExpect(status().isNotFound());

        verify(reviewStatisticsService, times(1)).getProductReviewSummary(invalidProductId);
    }
}