package com.cMall.feedShop.review.infrastructure.jpa;

import com.cMall.feedShop.review.domain.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 리뷰 이미지 JPA Repository
 */
public interface ReviewImageJpaRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * 리뷰별 이미지 목록 조회 (순서대로)
     */
    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review.reviewId = :reviewId ORDER BY ri.imageOrder ASC")
    List<ReviewImage> findByReviewIdOrderByImageOrder(@Param("reviewId") Long reviewId);

    /**
     * 리뷰별 이미지 개수
     */
    @Query("SELECT COUNT(ri) FROM ReviewImage ri WHERE ri.review.reviewId = :reviewId")
    Long countByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 리뷰별 모든 이미지 삭제
     */
    @Modifying
    @Query("DELETE FROM ReviewImage ri WHERE ri.review.reviewId = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 이미지 소유권 확인
     */
    @Query("SELECT COUNT(ri) > 0 FROM ReviewImage ri WHERE ri.imageId = :imageId AND ri.review.userId = :userId")
    boolean existsByImageIdAndReviewUserId(@Param("imageId") Long imageId, @Param("userId") Long userId);
}
