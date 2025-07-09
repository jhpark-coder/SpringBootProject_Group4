package com.creatorworks.nexus.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.order.entity.OrderItem;
import com.creatorworks.nexus.order.entity.OrderItem.ItemType;
import com.creatorworks.nexus.product.entity.Product;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 주문 ID로 주문 항목들을 조회합니다.
     */
    List<OrderItem> findByOrderId(Long orderId);
    
    /**
     * 상품별 주문 항목들을 조회합니다.
     */
    List<OrderItem> findByProduct(Product product);
    
    /**
     * 작가별 주문 항목들을 조회합니다.
     */
    List<OrderItem> findByAuthor(Member author);
    
    /**
     * 주문 항목 타입별로 조회합니다.
     */
    List<OrderItem> findByItemType(ItemType itemType);
    
    /**
     * 특정 상품의 총 판매 수량을 조회합니다.
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
           "WHERE oi.product = :product AND oi.order.orderStatus = 'COMPLETED'")
    Long getTotalQuantityByProduct(@Param("product") Product product);
    
    /**
     * 특정 작가의 총 구독 수를 조회합니다.
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi " +
           "WHERE oi.author = :author AND oi.itemType = 'SUBSCRIPTION' " +
           "AND oi.order.orderStatus = 'COMPLETED'")
    Long getTotalSubscriptionsByAuthor(@Param("author") Member author);
    
    /**
     * 특정 상품의 총 판매 금액을 조회합니다.
     */
    @Query("SELECT COALESCE(SUM(oi.price * oi.quantity), 0) FROM OrderItem oi " +
           "WHERE oi.product = :product AND oi.order.orderStatus = 'COMPLETED'")
    Long getTotalSalesAmountByProduct(@Param("product") Product product);
    
    /**
     * 특정 작가의 총 구독 수익을 조회합니다.
     */
    @Query("SELECT COALESCE(SUM(oi.price * oi.quantity), 0) FROM OrderItem oi " +
           "WHERE oi.author = :author AND oi.itemType = 'SUBSCRIPTION' " +
           "AND oi.order.orderStatus = 'COMPLETED'")
    Long getTotalSubscriptionRevenueByAuthor(@Param("author") Member author);
} 