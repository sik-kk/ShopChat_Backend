package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.review.application.ReviewService;
import com.cMall.feedShop.review.application.dto.response.ProductReviewSummaryResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@WebMvcTest(controllers = ReviewProductController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReviewProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    @DisplayName("상품별 리뷰 목록 조회 API 테스트")
    @WithMockUser
    void getProductReviews_success() throws Exception {
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

        when(reviewService.getProductReviews(eq(productId), any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/{productId}/reviews", productId)
                        .with(csrf())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(25))
                .andExpect(jsonPath("$.mostCommonSizeFit").value("PERFECT"))
                .andExpect(jsonPath("$.recentReviews.length()").value(2));

        verify(reviewService, times(1)).getProductReviews(eq(productId), any());
    }

    @Test
    @DisplayName("존재하지 않는 상품의 리뷰 조회")
    @WithMockUser
    void getProductReviews_productNotFound() throws Exception {
        // given
        Long productId = 999L;
        when(reviewService.getProductReviews(eq(productId), any()))
                .thenThrow(new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/products/{productId}/reviews", productId)
                        .with(csrf())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError()); // GlobalExceptionHandler에 의해 500으로 처리

        verify(reviewService, times(1)).getProductReviews(eq(productId), any());
    }

    @Test
    @DisplayName("잘못된 페이지 파라미터로 요청")
    @WithMockUser
    void getProductReviews_invalidPageParam() throws Exception {
        // given
        Long productId = 1L;

        // when & then - 음수 페이지
        mockMvc.perform(get("/api/products/{productId}/reviews", productId)
                        .with(csrf())
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError()); // Spring의 기본 validation에 의해 처리

        verify(reviewService, never()).getProductReviews(any(), any());
    }
}