package com.cMall.feedShop.review.integration;

import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.domain.entity.SizeFit;
import com.cMall.feedShop.review.domain.entity.Cushion;
import com.cMall.feedShop.review.domain.entity.Stability;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ReviewApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("API Integration: 완전한 리뷰 CRUD 워크플로우")
    void completeReviewCrudWorkflow() throws Exception {
        // 1. CREATE - 리뷰 생성
        ReviewCreateRequest createRequest = ReviewCreateRequest.builder()
                .userId(1L)
                .productId(1L)
                .reviewTitle("API 테스트 리뷰")
                .rating(5)
                .content("완벽한 신발입니다. 5단계 평가 모두 최고!")
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .imageUrls(new ArrayList<>())
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").exists())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.sizeFit").value("PERFECT"))
                .andExpect(jsonPath("$.cushioning").value("VERY_SOFT"))
                .andExpect(jsonPath("$.stability").value("VERY_STABLE"))
                .andDo(print())
                .andReturn();

        // 생성된 리뷰 ID 추출
        String responseContent = createResult.getResponse().getContentAsString();
        Long reviewId = objectMapper.readTree(responseContent).get("reviewId").asLong();

        // 2. READ - 리뷰 상세 조회
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId))
                .andExpect(jsonPath("$.reviewTitle").value("API 테스트 리뷰"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.sizeFit").value("PERFECT"))
                .andExpect(jsonPath("$.cushioning").value("VERY_SOFT"))
                .andExpect(jsonPath("$.stability").value("VERY_STABLE"))
                .andDo(print());

        // 3. READ - 사용자별 리뷰 목록 조회
        mockMvc.perform(get("/api/users/{userId}/reviews", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].reviewId").value(reviewId))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andDo(print());

        // 4. DELETE - 리뷰 삭제
        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .param("userId", "1"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    @DisplayName("API Integration: 상품별 리뷰 통계 및 요약 조회")
    void productReviewStatisticsAndSummary() throws Exception {
        Long productId = 1L;

        // 다양한 평점의 리뷰들 생성
        createReviewViaApi(1L, productId, 5, SizeFit.PERFECT, Cushion.VERY_SOFT, Stability.VERY_STABLE);
        createReviewViaApi(2L, productId, 4, SizeFit.PERFECT, Cushion.SOFT, Stability.STABLE);
        createReviewViaApi(3L, productId, 3, SizeFit.BIG, Cushion.NORMAL, Stability.NORMAL);
        createReviewViaApi(4L, productId, 2, SizeFit.SMALL, Cushion.FIRM, Stability.UNSTABLE);
        createReviewViaApi(5L, productId, 1, SizeFit.VERY_SMALL, Cushion.VERY_FIRM, Stability.VERY_UNSTABLE);

        // 상품 리뷰 요약 조회
        mockMvc.perform(get("/api/products/{productId}/reviews/summary", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.totalReviews").value(5))
                .andExpect(jsonPath("$.averageRating").value(3.0)) // (5+4+3+2+1)/5 = 3.0
                .andExpect(jsonPath("$.mostCommonSizeFit").exists())
                .andExpect(jsonPath("$.recentReviews").isArray())
                .andDo(print());

        // 상품 리뷰 통계 조회
        mockMvc.perform(get("/api/products/{productId}/reviews/statistics", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.totalReviews").value(5))
                .andExpect(jsonPath("$.averageRating").value(3.0))
                .andExpect(jsonPath("$.ratingDistribution.5").value(1))
                .andExpect(jsonPath("$.ratingDistribution.4").value(1))
                .andExpect(jsonPath("$.ratingDistribution.3").value(1))
                .andExpect(jsonPath("$.ratingDistribution.2").value(1))
                .andExpect(jsonPath("$.ratingDistribution.1").value(1))
                .andDo(print());
    }

    @Test
    @DisplayName("API Integration: 5단계 특성별 리뷰 필터링")
    void characteristicBasedFiltering() throws Exception {
        Long productId = 1L;

        // 다양한 특성의 리뷰들 생성
        createReviewViaApi(1L, productId, 5, SizeFit.PERFECT, Cushion.VERY_SOFT, Stability.VERY_STABLE);
        createReviewViaApi(2L, productId, 5, SizeFit.PERFECT, Cushion.VERY_SOFT, Stability.STABLE);
        createReviewViaApi(3L, productId, 4, SizeFit.BIG, Cushion.SOFT, Stability.NORMAL);

        // 사이즈 핏별 필터링 (가상의 엔드포인트)
        mockMvc.perform(get("/api/products/{productId}/reviews", productId)
                        .param("sizeFit", "PERFECT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2)) // PERFECT 사이즈 2개
                .andDo(print());

        // 쿠셔닝별 필터링 (가상의 엔드포인트)
        mockMvc.perform(get("/api/products/{productId}/reviews", productId)
                        .param("cushioning", "VERY_SOFT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2)) // VERY_SOFT 쿠셔닝 2개
                .andDo(print());
    }

    @Test
    @DisplayName("API Integration: 시간 경과에 따른 특성 변화 업데이트")
    void characteristicsChangeOverTime() throws Exception {
        // 초기 완벽한 리뷰 생성
        ReviewCreateRequest initialRequest = ReviewCreateRequest.builder()
                .userId(1L)
                .productId(1L)
                .reviewTitle("처음엔 완벽했던 신발")
                .rating(5)
                .content("처음엔 모든 게 완벽했어요")
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .imageUrls(new ArrayList<>())
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("reviewId").asLong();

        // 시간 경과 후 특성 변화 업데이트
        com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest updateRequest =
                com.cMall.feedShop.review.application.dto.request.ReviewUpdateRequest.builder()
                        .reviewTitle("한 달 후 재평가 - 많이 변했어요")
                        .rating(2)
                        .content("신발이 늘어나고 쿠션이 주저앉아서 많이 아쉬워졌어요")
                        .sizeFit(SizeFit.BIG)
                        .cushioning(Cushion.FIRM)
                        .stability(Stability.UNSTABLE)
                        .build();

        // 업데이트 수행
        mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andDo(print());

        // 업데이트된 내용 확인
        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewTitle").value("한 달 후 재평가 - 많이 변했어요"))
                .andExpect(jsonPath("$.rating").value(2))
                .andExpect(jsonPath("$.sizeFit").value("BIG"))
                .andExpect(jsonPath("$.cushioning").value("FIRM"))
                .andExpect(jsonPath("$.stability").value("UNSTABLE"))
                .andExpect(jsonPath("$.content").value(containsString("주저앉아서")))
                .andDo(print());
    }

    @Test
    @DisplayName("API Integration: 에러 케이스 처리")
    void errorCasesHandling() throws Exception {
        // 1. 존재하지 않는 리뷰 조회
        mockMvc.perform(get("/api/reviews/{reviewId}", 99999L))
                .andExpect(status().isNotFound())
                .andDo(print());

        // 2. 잘못된 상품 ID로 통계 조회
        mockMvc.perform(get("/api/products/{productId}/reviews/statistics", 99999L))
                .andExpect(status().isNotFound())
                .andDo(print());

        // 3. 중복 리뷰 생성 시도
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .userId(1L)
                .productId(1L)
                .reviewTitle("첫 번째 리뷰")
                .rating(5)
                .content("완벽한 신발")
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .imageUrls(new ArrayList<>())
                .build();

        // 첫 번째 리뷰 생성
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 같은 사용자가 같은 상품에 중복 리뷰 시도
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("이미 해당 상품에 대한 리뷰를 작성하셨습니다")))
                .andDo(print());
    }

    // 헬퍼 메서드
    private void createReviewViaApi(Long userId, Long productId, int rating,
                                    SizeFit sizeFit, Cushion cushioning, Stability stability) throws Exception {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .userId(userId)
                .productId(productId)
                .reviewTitle("테스트 리뷰 " + userId)
                .rating(rating)
                .content("평점 " + rating + "점 리뷰입니다")
                .sizeFit(sizeFit)
                .cushioning(cushioning)
                .stability(stability)
                .imageUrls(new ArrayList<>())
                .build();

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}