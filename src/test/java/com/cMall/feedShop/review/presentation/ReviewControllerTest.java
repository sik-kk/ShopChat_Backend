package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.review.application.ReviewService;
import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.domain.entity.SizeFit;
import com.cMall.feedShop.review.domain.entity.Cushion;
import com.cMall.feedShop.review.domain.entity.Stability;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    // RE-01: 5단계 특성을 포함한 신발 리뷰 작성 API 테스트
    @Test
    @DisplayName("Given 5-level characteristics request_When post review_Then return 201 with all levels")
    void given5LevelCharacteristicsRequest_whenPostReview_thenReturn201WithAllLevels() throws Exception {
        // given
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .content("정말 편한 신발입니다. 사이즈 딱 맞고 쿠션 매우 부드럽고 안정감 최고예요")
                .rating(5)
                .userId(1L)
                .productId(1L)
                .sizeFit(SizeFit.PERFECT)      // 딱 맞음
                .cushioning(Cushion.VERY_SOFT) // 매우 부드러움
                .stability(Stability.VERY_STABLE) // 매우 안정적
                .build();

        ReviewCreateResponse response = ReviewCreateResponse.builder()
                .reviewId(1L)
                .content(request.getContent())
                .rating(5)
                .sizeFit(SizeFit.PERFECT)
                .cushioning(Cushion.VERY_SOFT)
                .stability(Stability.VERY_STABLE)
                .build();

        when(reviewService.createReview(any(ReviewCreateRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(1))
                .andExpect(jsonPath("$.sizeFit").value("PERFECT"))
                .andExpect(jsonPath("$.cushioning").value("VERY_SOFT"))
                .andExpect(jsonPath("$.stability").value("VERY_STABLE"));

        verify(reviewService, times(1)).createReview(any(ReviewCreateRequest.class));
    }
}