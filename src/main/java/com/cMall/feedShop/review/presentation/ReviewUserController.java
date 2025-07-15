package com.cMall.feedShop.review.presentation;

import com.cMall.feedShop.review.application.dto.request.ReviewCreateRequest;
import com.cMall.feedShop.review.application.dto.response.ReviewCreateResponse;
import com.cMall.feedShop.review.application.ReviewService;
import com.cMall.feedShop.common.aop.ApiResponseFormat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

/**
 * SPRINT1 - 사용자 중심 리뷰 API 컨트롤러
 * RE-01: 리뷰 작성
 */
@Slf4j
@RestController
@RequestMapping("/api/users/{userId}/reviews")
@RequiredArgsConstructor
@Tag(name = "User Review", description = "사용자 중심 리뷰 API - SPRINT1")
public class ReviewUserController {

    private final ReviewService reviewService;

    /**
     * RE-01: 리뷰 작성
     * 새로운 리뷰를 작성합니다.
     */
    @ApiResponseFormat(message = "리뷰가 성공적으로 등록되었습니다.")
    @PostMapping
    @PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
    @Operation(summary = "리뷰 등록", description = "새로운 리뷰를 등록합니다.")
    public ReviewCreateResponse createReview(
            @PathVariable @Min(1) Long userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        
        log.info("리뷰 등록 요청 - 사용자: {}, 상품: {}", userId, request.getProductId());
        return reviewService.createReview(request);
    }
}