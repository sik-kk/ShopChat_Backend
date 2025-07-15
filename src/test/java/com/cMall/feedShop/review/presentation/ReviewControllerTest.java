package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.review.application.ReviewService;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewDetailResponse;
import com.cMall.feedShop.review.domain.entity.SizeFit;
import com.cMall.feedShop.review.domain.entity.Cushion;
import com.cMall.feedShop.review.domain.entity.Stability;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// SecurityConfig를 제외하고 테스트하려면 다음과 같이 설정
@WebMvcTest(controllers = ReviewController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("리뷰 상세 조회 API 테스트")
    @WithMockUser
    void getReviewDetail_success() throws Exception {
        // given
        Long reviewId = 1L;
        ReviewDetailResponse response = ReviewDetailResponse.builder()
                .reviewId(reviewId)
                .productId(1L)
                .userId(1L)
                .userName("테스트사용자")
                .reviewTitle("테스트 리뷰")
                .rating(5)
                .content("정말 좋은 신발입니다")
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .imageUrls(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(reviewService.getReviewDetail(reviewId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewId").value(reviewId))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.sizeFit").value("PERFECT"))
                .andExpect(jsonPath("$.data.cushioning").value("VERY_SOFT"))
                .andExpect(jsonPath("$.data.stability").value("VERY_STABLE"));

        verify(reviewService, times(1)).getReviewDetail(reviewId);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 조회 시 예외 처리")
    @WithMockUser
    void getReviewDetail_notFound() throws Exception {
        // given
        Long reviewId = 999L;
        when(reviewService.getReviewDetail(reviewId))
                .thenThrow(new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError()); // GlobalExceptionHandler에 의해 500으로 처리

        verify(reviewService, times(1)).getReviewDetail(reviewId);
    }

    @Test
    @DisplayName("잘못된 reviewId 형식으로 요청 시 400 에러")
    @WithMockUser
    void getReviewDetail_invalidId() throws Exception {
        // when & then
        mockMvc.perform(get("/api/reviews/{reviewId}", "invalid")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).getReviewDetail(any());
    }
}