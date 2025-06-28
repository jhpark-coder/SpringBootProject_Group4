package com.creatorworks.nexus.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.creatorworks.nexus.product.entity.ProductInquiry;

public interface ProductInquiryRepository extends JpaRepository<ProductInquiry, Long> {

    /**
     * 특정 상품에 달린 모든 문의와 답변을 작성일 순으로 조회합니다.
     * 부모가 없는 문의(최상위 글)가 먼저 오고, 그 다음에 각 문의에 대한 답변들이 정렬됩니다.
     * @param productId 상품 ID
     * @return 정렬된 문의 목록
     */
    List<ProductInquiry> findByProduct_IdOrderByParent_IdAscRegTimeAsc(Long productId);

} 