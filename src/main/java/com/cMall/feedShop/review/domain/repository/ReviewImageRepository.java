package com.cMall.feedShop.review.domain.repository;

import com.cMall.feedShop.review.domain.entity.ReviewImage;

import java.util.List;
import java.util.Optional;

/**
 * 리뷰 이미지 도메인 Repository 인터페이스
 */
public interface ReviewImageRepository {

    /**
     * 이미지 저장
     */
    ReviewImage save(ReviewImage reviewImage);

    /**
     * ID로 이미지 조회
     */
    Optional<ReviewImage> findById(Long imageId);

    /**
     * 리뷰별 이미지 목록 조회 (순서대로)
     */
    List<ReviewImage> findByReviewIdOrderByImageOrder(Long reviewId);

    /**
     * 리뷰별 이미지 개수 조회
     */
    Long countByReviewId(Long reviewId);

    /**
     * 이미지 삭제
     */
    void deleteById(Long imageId);

    /**
     * 리뷰별 모든 이미지 삭제
     */
    void deleteByReviewId(Long reviewId);

    /**
     * 이미지 소유권 확인
     */
    boolean existsByIdAndReviewUserId(Long imageId, Long userId);
}