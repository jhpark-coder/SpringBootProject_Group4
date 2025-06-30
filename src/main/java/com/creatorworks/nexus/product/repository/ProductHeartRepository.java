package com.creatorworks.nexus.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.product.entity.ProductHeart;

public interface ProductHeartRepository extends JpaRepository<ProductHeart, Long> {
    
    // 특정 사용자가 특정 상품을 좋아요했는지 확인
    @Query("SELECT ph FROM ProductHeart ph WHERE ph.member.id = :memberId AND ph.product.id = :productId")
    Optional<ProductHeart> findByMemberIdAndProductId(@Param("memberId") Long memberId, @Param("productId") Long productId);
    
    // 특정 상품의 좋아요 개수 조회
    @Query("SELECT COUNT(ph) FROM ProductHeart ph WHERE ph.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
    
    // 특정 사용자가 좋아요한 상품 개수 조회
    @Query("SELECT COUNT(ph) FROM ProductHeart ph WHERE ph.member.id = :memberId")
    long countByMemberId(@Param("memberId") Long memberId);
} 