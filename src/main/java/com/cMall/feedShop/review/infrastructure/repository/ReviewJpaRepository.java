package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import com.cMall.feedShop.review.domain.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    // ========== 기존 쿼리들 ==========

    // 상품별 활성 리뷰 조회 (최신순)
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false ORDER BY r.createdAt DESC")
    Page<Review> findActiveReviewsByProductId(@Param("productId") Long productId, Pageable pageable);

    // 상품별 활성 리뷰 조회 (점수순)
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false ORDER BY r.points DESC, r.createdAt DESC")
    Page<Review> findActiveReviewsByProductIdOrderByPoints(@Param("productId") Long productId, Pageable pageable);

    // 상품별 평균 평점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // 상품별 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false")
    Long countActiveReviewsByProductId(@Param("productId") Long productId);

    // ========== 새로 추가: 3요소 통계 쿼리들 ==========

    @Query("SELECT r.cushion, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false GROUP BY r.cushion")
    List<Object[]> findCushionDistributionByProductId(@Param("productId") Long productId);

    @Query("SELECT r.sizeFit, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false GROUP BY r.sizeFit")
    List<Object[]> findSizeFitDistributionByProductId(@Param("productId") Long productId);

    @Query("SELECT r.stability, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE' AND r.isBlinded = false GROUP BY r.stability")
    List<Object[]> findStabilityDistributionByProductId(@Param("productId") Long productId);
}