package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.review.application.ReviewService;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewDetailResponse;
import com.cMall.feedShop.review.application.dto.response.ReviewSummaryResponse;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@WebMvcTest(ReviewUserController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReviewUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Given user id_When get user reviews_Then return review list")
    void givenUserId_whenGetUserReviews_thenReturnReviewList() throws Exception {
        // given
        Long userId = 1L;
        List<ReviewDetailResponse> responses = List.of(
                ReviewDetailResponse.builder()
                        .reviewId(1L)
                        .productId(1L)
                        .userId(userId)
                        .userName("사용자1")
                        .reviewTitle("좋은 상품입니다")
                        .rating(5)
                        .content("정말 만족스러운 구매였습니다")
                        .sizeFit(SizeFit.PERFECT)
                        .cushioning(Cushion.VERY_SOFT)
                        .stability(Stability.VERY_STABLE)
                        .imageUrls(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                ReviewDetailResponse.builder()
                        .reviewId(2L)
                        .productId(2L)
                        .userId(userId)
                        .userName("사용자1")
                        .reviewTitle("괜찮은 상품")
                        .rating(4)
                        .content("전반적으로 만족합니다")
                        .sizeFit(SizeFit.BIG)
                        .cushioning(Cushion.SOFT)
                        .stability(Stability.STABLE)
                        .imageUrls(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        Page<ReviewDetailResponse> pageResponse = new PageImpl<>(responses, PageRequest.of(0, 10), responses.size());
        when(reviewService.getUserReviews(eq(userId), any())).thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/users/{userId}/reviews", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].reviewId").value(1))
                .andExpect(jsonPath("$.content[0].rating").value(5))
                .andExpect(jsonPath("$.content[1].reviewId").value(2))
                .andExpect(jsonPath("$.content[1].rating").value(4));

        verify(reviewService, times(1)).getUserReviews(eq(userId), any());
    }

    @Test
    @DisplayName("Given review data_When create review_Then return created review")
    void givenReviewData_whenCreateReview_thenReturnCreatedReview() throws Exception {
        // given
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .userId(1L)
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
                .userId(1L)
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
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.reviewTitle").value("훌륭한 상품!"));

        verify(reviewService, times(1)).createReview(any(ReviewCreateRequest.class));
    }
/*
    @Test
    @DisplayName("Given review id_When delete review_Then return no content")
    void givenReviewId_whenDeleteReview_thenReturnNoContent() throws Exception {
        // given
        Long userId = 1L;
        Long reviewId = 1L;

        doNothing().when(reviewService).deleteReview(userId, reviewId);

        // when & then
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).deleteReview(userId, reviewId);
    }
*/
    @Test
    @DisplayName("Given invalid user id_When get user reviews_Then return empty list")
    void givenInvalidUserId_whenGetUserReviews_thenReturnEmptyList() throws Exception {
        // given
        Long invalidUserId = 999L;
        Page<ReviewDetailResponse> emptyPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);

        when(reviewService.getUserReviews(eq(invalidUserId), any())).thenReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/users/{userId}/reviews", invalidUserId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(reviewService, times(1)).getUserReviews(eq(invalidUserId), any());
    }
}