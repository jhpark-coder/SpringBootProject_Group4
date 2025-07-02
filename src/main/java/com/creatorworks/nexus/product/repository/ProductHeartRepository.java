package com.creatorworks.nexus.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
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

    // 특정 사용자가 좋아요 한 모든 ProductHeart 엔티티 조회
    List<ProductHeart> findByMember(Member member);
    
    /**
     * [수정] 가장 좋아요를 많이 받은 상품 ID 목록 조회 (비로그인 사용자용)
     */
    @Query("SELECT ph.product.id FROM ProductHeart ph GROUP BY ph.product.id ORDER BY COUNT(ph.product.id) DESC")
    List<Long> findTopHeartedProductIds(Pageable pageable);

    /**
     * [추가] 특정 상품들을 제외하고 가장 좋아요를 많이 받은 상품 ID 목록 조회 (로그인 사용자 추천 채우기용)
     */
    @Query("SELECT ph.product.id FROM ProductHeart ph " +
            "WHERE ph.product.id NOT IN :excludedIds " +
            "GROUP BY ph.product.id ORDER BY COUNT(ph.product.id) DESC")
    List<Long> findTopHeartedProductIds(@Param("excludedIds") List<Long> excludedIds, Pageable pageable);

} 