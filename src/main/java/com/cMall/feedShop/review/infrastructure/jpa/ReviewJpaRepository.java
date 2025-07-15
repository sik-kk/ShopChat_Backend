package com.cMall.feedShop.review.infrastructure.jpa;

import com.cMall.feedShop.review.domain.entity.Review;
import com.cMall.feedShop.review.domain.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
    
    // 상품별 리뷰 조회 (상태별, 생성일 내림차순)
    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(
        Long productId, 
        ReviewStatus status, 
        Pageable pageable
    );
    
    // 사용자별 리뷰 조회 (상태별, 생성일 내림차순)
    Page<Review> findByUserIdAndStatusOrderByCreatedAtDesc(
        Long userId, 
        ReviewStatus status, 
        Pageable pageable
    );
    
    // 상품별 리뷰 개수 조회
    Long countByProductIdAndStatus(Long productId, ReviewStatus status);
    
    // 리뷰 ID로 조회 (삭제되지 않은 것만)
    @Query("SELECT r FROM Review r WHERE r.reviewId = :reviewId AND r.status != 'DELETED'")
    Optional<Review> findByReviewIdAndStatusNotDeleted(@Param("reviewId") Long reviewId);
    
    // 사용자가 해당 상품에 대해 활성 리뷰를 작성했는지 확인
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.userId = :userId AND r.productId = :productId AND r.status = 'ACTIVE'")
    boolean existsByUserIdAndProductIdAndStatusActive(@Param("userId") Long userId, @Param("productId") Long productId);
    
    // 리뷰 ID와 사용자 ID로 소유권 확인
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.reviewId = :reviewId AND r.userId = :userId")
    boolean existsByReviewIdAndUserId(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    
    // 상품별 평균 평점 조회
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = 'ACTIVE'")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    Long countByProductIdAndStatusAndRating(Long productId, ReviewStatus status, Integer rating);
}