package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    // ========== 기존 메서드들 ==========

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return reviewJpaRepository.findById(reviewId);
    }

    @Override
    public void delete(Review review) {
        reviewJpaRepository.delete(review);
    }

    @Override
    public Page<Review> findActiveReviewsByProductId(Long productId, Pageable pageable) {
        return reviewJpaRepository.findActiveReviewsByProductId(productId, pageable);
    }

    @Override
    public Page<Review> findActiveReviewsByProductIdOrderByPoints(Long productId, Pageable pageable) {
        return reviewJpaRepository.findActiveReviewsByProductIdOrderByPoints(productId, pageable);
    }

    @Override
    public Double findAverageRatingByProductId(Long productId) {
        return reviewJpaRepository.findAverageRatingByProductId(productId);
    }

    @Override
    public Long countActiveReviewsByProductId(Long productId) {
        return reviewJpaRepository.countActiveReviewsByProductId(productId);
    }

    // ========== 새로 추가: 3요소 통계 메서드들 ==========

    @Override
    public Map<Cushion, Long> getCushionDistributionByProductId(Long productId) {
        List<Object[]> results = reviewJpaRepository.findCushionDistributionByProductId(productId);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Cushion) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<SizeFit, Long> getSizeFitDistributionByProductId(Long productId) {
        List<Object[]> results = reviewJpaRepository.findSizeFitDistributionByProductId(productId);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (SizeFit) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Override
    public Map<Stability, Long> getStabilityDistributionByProductId(Long productId) {
        List<Object[]> results = reviewJpaRepository.findStabilityDistributionByProductId(productId);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Stability) result[0],
                        result -> (Long) result[1]
                ));
    }
}