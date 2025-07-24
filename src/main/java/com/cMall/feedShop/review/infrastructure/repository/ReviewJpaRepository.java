package com.cMall.feedShop.review.infrastructure.repository;

import com.cMall.feedShop.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
    // 기본 CRUD 메서드만 제공
    // 복잡한 쿼리는 QueryDSL로 처리
}