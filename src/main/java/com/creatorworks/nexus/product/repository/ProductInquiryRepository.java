package com.creatorworks.nexus.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.entity.ProductInquiry;
import com.creatorworks.nexus.member.entity.Member;

public interface ProductInquiryRepository extends JpaRepository<ProductInquiry, Long> {

    /**
     * 특정 상품에 달린 모든 문의와 답변을 작성일 순으로 조회합니다.
     * 부모가 없는 문의(최상위 글)가 먼저 오고, 그 다음에 각 문의에 대한 답변들이 정렬됩니다.
     * @param productId 상품 ID
     * @return 정렬된 문의 목록
     */
    List<ProductInquiry> findByProduct_IdOrderByParent_IdAscRegTimeAsc(Long productId);

    Page<ProductInquiry> findByProductAndParentIsNull(Product product, Pageable pageable);

    /**
     * 판매자가 등록한 모든 상품에 대한 (최상위) 문의 목록을 조회합니다.
     * 답변을 제외하고 질문 글(parent null)만 가져옵니다.
     */
    @Query("SELECT i FROM ProductInquiry i WHERE i.product.seller = :seller AND i.parent IS NULL")
    Page<ProductInquiry> findTopLevelBySeller(@Param("seller") Member seller, Pageable pageable);

} 