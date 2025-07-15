package com.cMall.feedShop.review.infrastructure;

import com.cMall.feedShop.review.domain.entity.ReviewImage;
import com.cMall.feedShop.review.domain.repository.ReviewImageRepository; // 🔥 도메인 인터페이스 import
import com.cMall.feedShop.review.infrastructure.jpa.ReviewImageJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 리뷰 이미지 Repository 구현체
 * Domain의 ReviewImageRepository 인터페이스를 Infrastructure에서 구현
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ReviewImageRepositoryImpl implements ReviewImageRepository { // 🔥 도메인 인터페이스 구현

    private final ReviewImageJpaRepository jpaRepository; // JPA Repository 사용

    @Override
    public ReviewImage save(ReviewImage reviewImage) {
        log.debug("리뷰 이미지 저장 - imageId: {}", reviewImage.getImageId());
        return jpaRepository.save(reviewImage);
    }

    @Override
    public Optional<ReviewImage> findById(Long imageId) {
        log.debug("리뷰 이미지 조회 - imageId: {}", imageId);
        return jpaRepository.findById(imageId);
    }

    @Override
    public List<ReviewImage> findByReviewIdOrderByImageOrder(Long reviewId) {
        log.debug("리뷰별 이미지 목록 조회 - reviewId: {}", reviewId);
        return jpaRepository.findByReviewIdOrderByImageOrder(reviewId);
    }

    @Override
    public Long countByReviewId(Long reviewId) {
        log.debug("리뷰별 이미지 개수 조회 - reviewId: {}", reviewId);
        return jpaRepository.countByReviewId(reviewId);
    }

    @Override
    public void deleteById(Long imageId) {
        log.debug("리뷰 이미지 삭제 - imageId: {}", imageId);
        jpaRepository.deleteById(imageId);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        log.debug("리뷰별 모든 이미지 삭제 - reviewId: {}", reviewId);
        jpaRepository.deleteByReviewId(reviewId);
    }

    @Override
    public boolean existsByIdAndReviewUserId(Long imageId, Long userId) {
        log.debug("이미지 소유권 확인 - imageId: {}, userId: {}", imageId, userId);
        return jpaRepository.existsByImageIdAndReviewUserId(imageId, userId);
    }
}