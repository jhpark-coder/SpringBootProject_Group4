package com.creatorworks.nexus.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.order.dto.AgeRatioDto;
import com.creatorworks.nexus.order.dto.GenderRatioDto;
import com.creatorworks.nexus.order.dto.MonthlySalesDto;
import com.creatorworks.nexus.order.dto.TopSellingProductDto;
import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Order.OrderStatus;
import com.creatorworks.nexus.order.entity.Order.OrderType;
import com.creatorworks.nexus.product.entity.Product;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // === 새로운 통합 주문 관련 쿼리 ===
    
    /**
     * 구매자의 주문 목록을 조회합니다.
     */
    Page<Order> findByBuyerOrderByOrderDateDesc(Member buyer, Pageable pageable);
    
    /**
     * 구매자의 특정 타입 주문 목록을 조회합니다.
     */
    Page<Order> findByBuyerAndOrderTypeOrderByOrderDateDesc(Member buyer, OrderType orderType, Pageable pageable);
    
    /**
     * 주문 상태별 주문 목록을 조회합니다.
     */
    List<Order> findByOrderStatus(OrderStatus orderStatus);
    
    /**
     * 특정 기간 동안의 주문 목록을 조회합니다.
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * impUid로 주문을 조회합니다.
     */
    Optional<Order> findByPayment_ImpUid(String impUid);
    
    /**
     * merchantUid로 주문을 조회합니다.
     */
    Optional<Order> findByPayment_MerchantUid(String merchantUid);
    
    /**
     * customerUid로 정기결제 주문을 조회합니다.
     */
    Optional<Order> findByPayment_CustomerUid(String customerUid);
    
    /**
     * 다음 결제일이 임박한 구독 주문을 조회합니다.
     */
    @Query("SELECT o FROM Order o WHERE o.orderType = 'SUBSCRIPTION' " +
           "AND o.orderStatus = 'COMPLETED' " +
           "AND o.payment.nextBillingDate BETWEEN :now AND :threeDaysLater")
    List<Order> findUpcomingSubscriptionOrders(@Param("now") LocalDateTime now,
                                              @Param("threeDaysLater") LocalDateTime threeDaysLater);

    // === 기존 상품 구매 관련 쿼리 (호환성 유지) ===
    
    /**
     * 특정 사용자가 특정 상품을 구매했는지 여부를 확인합니다.
     * @param buyer 구매자
     * @param product 상품
     * @return 구매했다면 true, 아니면 false
     */
    @Query("SELECT EXISTS(SELECT 1 FROM Order o JOIN o.orderItems oi " +
           "WHERE o.buyer = :buyer AND oi.product = :product AND o.orderStatus = 'COMPLETED')")
    boolean existsByBuyerAndProduct(@Param("buyer") Member buyer, @Param("product") Product product);
    
    /**
     * 특정 상품의 구매 횟수를 조회합니다.
     * @param productId 상품 ID
     * @return 구매 횟수
     */
    @Query("SELECT COUNT(oi) FROM Order o JOIN o.orderItems oi " +
           "WHERE oi.product.id = :productId AND o.orderStatus = 'COMPLETED'")
    long countByProductId(@Param("productId") Long productId);

    /**
     * 특정 사용자의 전체 주문 건수를 조회합니다.
     * @param buyer 구매자 (Member 엔티티)
     * @return 해당 사용자의 주문 건수
     */
    long countByBuyer(Member buyer);



    /**
     * 특정 기간 동안 특정 판매자의 상품 중 가장 많이 팔린 상품 목록을 조회합니다.
     * @param seller 판매자(작가) Member 엔티티
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보 (상위 N개)
     * @return TopSellingProductDto 목록
     */
    @Query("SELECT new com.creatorworks.nexus.order.dto.TopSellingProductDto(" +
            "  p.id, p.name, p.imageUrl, p.seller.name, COUNT(oi.id)) " +
            "FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
            "WHERE p.seller = :seller " +
            "  AND o.orderDate BETWEEN :startDate AND :endDate " +
            "  AND o.orderStatus = 'COMPLETED' " +
            "GROUP BY p.id, p.name, p.imageUrl, p.seller.name " +
            "ORDER BY COUNT(oi.id) DESC, p.name ASC")
    List<TopSellingProductDto> findTopSellingProductsBySeller(
            @Param("seller") Member seller,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // --- 통계용 쿼리 3종 추가 ---

    // 1. 월별 판매 현황 조회
    @Query("SELECT new com.creatorworks.nexus.order.dto.MonthlySalesDto(" +
            "  YEAR(o.orderDate), MONTH(o.orderDate), COUNT(o.id)) " +
            "FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
            "WHERE p.seller = :seller AND o.orderDate >= :startDate " +
            "  AND o.orderStatus = 'COMPLETED' " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
            "ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<MonthlySalesDto> findMonthlySalesBySeller(@Param("seller") Member seller, @Param("startDate") LocalDateTime startDate);

    // 2. 구매자 성별 비율 조회
    @Query("SELECT new com.creatorworks.nexus.order.dto.GenderRatioDto(" +
            "  o.buyer.gender, COUNT(DISTINCT o.buyer.id)) " +
            "FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
            "WHERE p.seller = :seller AND o.orderStatus = 'COMPLETED' " +
            "GROUP BY o.buyer.gender")
    List<GenderRatioDto> findGenderRatioBySeller(@Param("seller") Member seller);

    // 3. 구매자 나이대 비율 조회
    @Query("SELECT new com.creatorworks.nexus.order.dto.AgeRatioDto(" +
            "  CASE " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 50 THEN '50대 이상' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 40 THEN '40대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 30 THEN '30대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 20 THEN '20대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 10 THEN '10대' " +
            "    ELSE '기타' END, " +
            "  COUNT(DISTINCT o.buyer.id)) " +
            "FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
            "WHERE p.seller = :seller AND o.buyer.birthYear != 'N/A' " +
            "  AND o.orderStatus = 'COMPLETED' " +
            "GROUP BY CASE " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 50 THEN '50대 이상' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 40 THEN '40대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 30 THEN '30대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 20 THEN '20대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 10 THEN '10대' " +
            "    ELSE '기타' END")
    List<AgeRatioDto> findAgeRatioBySeller(@Param("seller") Member seller);

    // ★★★ 특정 판매자의 총 판매 건수를 조회하는 메서드 추가 ★★★
    @Query("SELECT COUNT(o) FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
           "WHERE p.seller = :seller AND o.orderStatus = 'COMPLETED'")
    long countByProductSeller(@Param("seller") Member seller);

    /**
     * [수정] 가장 많이 팔린 상품 ID 목록 조회 (비로그인 사용자용)
     */
    @Query("SELECT oi.product.id FROM Order o JOIN o.orderItems oi " +
           "WHERE o.orderStatus = 'COMPLETED' " +
           "GROUP BY oi.product.id ORDER BY COUNT(oi.product.id) DESC")
    List<Long> findTopSellingProductIds(Pageable pageable);

    /**
     * [추가] 특정 상품들을 제외하고 가장 많이 팔린 상품 ID 목록 조회 (로그인 사용자 추천 채우기용)
     */
    @Query("SELECT oi.product.id FROM Order o JOIN o.orderItems oi " +
            "WHERE oi.product.id NOT IN :excludedIds AND o.orderStatus = 'COMPLETED' " +
            "GROUP BY oi.product.id ORDER BY COUNT(oi.product.id) DESC")
    List<Long> findTopSellingProductIds(@Param("excludedIds") List<Long> excludedIds, Pageable pageable);

    List<Order> findByBuyer(Member member);
}
