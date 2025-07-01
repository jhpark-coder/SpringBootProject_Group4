package com.creatorworks.nexus.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.product.entity.Product;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 특정 사용자가 특정 상품을 구매했는지 여부를 확인합니다.
     * @param buyer 구매자
     * @param product 상품
     * @return 구매했다면 true, 아니면 false
     */
    boolean existsByBuyerAndProduct(Member buyer, Product product);
    
    /**
     * 특정 상품의 구매 횟수를 조회합니다.
     * @param productId 상품 ID
     * @return 구매 횟수
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
}
