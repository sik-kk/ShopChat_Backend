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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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

@WebMvcTest(controllers = ReviewUserController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReviewUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("리뷰 생성 API 테스트")
    @WithMockUser
    void createReview_success() throws Exception {
        // given
        Long userId = 1L;
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .userId(userId)
                .productId(1L)
                .reviewTitle("훌륭한 상품!")
                .rating(5)
                .content("정말 좋은 상품입니다. 추천해요!")
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .imageUrls(new ArrayList<>())
                .build();

        ReviewCreateResponse response = ReviewCreateResponse.builder()
                .reviewId(1L)
                .productId(1L)
                .userId(userId)
                .reviewTitle("훌륭한 상품!")
                .rating(5)
                .content("정말 좋은 상품입니다. 추천해요!")
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .imageUrls(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.createReview(any(ReviewCreateRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/users/{userId}/reviews", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewId").value(1))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.reviewTitle").value("훌륭한 상품!"));

        verify(reviewService, times(1)).createReview(any(ReviewCreateRequest.class));
    }

    @Test
    @DisplayName("유효하지 않은 리뷰 데이터로 생성 시도")
    @WithMockUser
    void createReview_invalidData() throws Exception {
        // given - rating이 범위를 벗어난 경우
        Long userId = 1L;
        ReviewCreateRequest invalidRequest = ReviewCreateRequest.builder()
                .userId(userId)
                .productId(1L)
                .reviewTitle("테스트")
                .rating(10) // 잘못된 평점
                .content("내용")
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .imageUrls(new ArrayList<>())
                .build();

        // when & then
        mockMvc.perform(post("/api/users/{userId}/reviews", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).createReview(any(ReviewCreateRequest.class));
    }

    @Test
    @DisplayName("중복 리뷰 생성 시 예외 처리")
    @WithMockUser
    void createReview_duplicate() throws Exception {
        // given
        Long userId = 1L;
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .userId(userId)
                .productId(1L)
                .reviewTitle("리뷰")
                .rating(5)
                .content("내용")
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .imageUrls(new ArrayList<>())
                .build();

        when(reviewService.createReview(any(ReviewCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("이미 해당 상품에 대한 리뷰를 작성하셨습니다."));

        // when & then
        mockMvc.perform(post("/api/users/{userId}/reviews", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // GlobalExceptionHandler에 의해 500으로 처리

        verify(reviewService, times(1)).createReview(any(ReviewCreateRequest.class));
    }
}