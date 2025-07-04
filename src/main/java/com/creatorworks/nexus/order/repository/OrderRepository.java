package com.creatorworks.nexus.order.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.creatorworks.nexus.order.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    /**
     * 특정 사용자의 전체 주문 건수를 조회합니다.
     * @param buyer 구매자 (Member 엔티티)
     * @return 해당 사용자의 주문 건수
     */
    long countByBuyer(Member buyer);
    // ----------------------------

    /**
     * 특정 사용자의 주문 목록을 최신순으로 조회합니다.
     * @param buyer 구매자
     * @param pageable 페이징 정보 (예: 상위 4개만 가져오기)
     * @return 최신순으로 정렬된 주문 목록
     */
    List<Order> findByBuyerOrderByOrderDateDesc(Member buyer, Pageable pageable);

    /**
     * 특정 기간 동안 특정 판매자의 상품 중 가장 많이 팔린 상품 목록을 조회합니다.
     * @param seller 판매자(작가) Member 엔티티
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보 (상위 N개)
     * @return TopSellingProductDto 목록
     */
    @Query("SELECT new com.creatorworks.nexus.order.dto.TopSellingProductDto(" +
            "  p.id, p.name, p.imageUrl, p.seller.name, COUNT(o.id)) " +
            "FROM Order o JOIN o.product p " +
            "WHERE p.seller = :seller " +
            "  AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.id, p.name, p.imageUrl, p.seller.name " +
            "ORDER BY COUNT(o.id) DESC, p.name ASC")
    List<TopSellingProductDto> findTopSellingProductsBySeller(
            @Param("seller") Member seller,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    // --- 통계용 쿼리 3종 추가 ---

    // 1. 월별 판매 현황 조회
    @Query("SELECT new com.creatorworks.nexus.order.dto.MonthlySalesDto(" +
            "  YEAR(o.orderDate), MONTH(o.orderDate), COUNT(o.id)) " + // SUM(p.price) -> COUNT(o.id)
            "FROM Order o JOIN o.product p " +
            "WHERE p.seller = :seller AND o.orderDate >= :startDate " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
            "ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<MonthlySalesDto> findMonthlySalesBySeller(@Param("seller") Member seller, @Param("startDate") LocalDateTime startDate);

    // 2. 구매자 성별 비율 조회
    @Query("SELECT new com.creatorworks.nexus.order.dto.GenderRatioDto(" + // product.dto -> order.dto
            "  o.buyer.gender, COUNT(DISTINCT o.buyer.id)) " +
            "FROM Order o JOIN o.product p " +
            "WHERE p.seller = :seller " +
            "GROUP BY o.buyer.gender")
    List<GenderRatioDto> findGenderRatioBySeller(@Param("seller") Member seller);

    // 3. 구매자 나이대 비율 조회
    @Query("SELECT new com.creatorworks.nexus.order.dto.AgeRatioDto(" +
            "  CASE " +
            // MySQL과 H2 모두에서 잘 동작하는 CAST( ... AS INT) 또는 CAST( ... AS SIGNED) 사용
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 50 THEN '50대 이상' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 40 THEN '40대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 30 THEN '30대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 20 THEN '20대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 10 THEN '10대' " +
            "    ELSE '기타' END, " +
            "  COUNT(DISTINCT o.buyer.id)) " +
            "FROM Order o JOIN o.product p " +
            "WHERE p.seller = :seller AND o.buyer.birthYear != 'N/A' " +
            // GROUP BY 절도 SELECT 절의 CASE 문과 완전히 동일해야 합니다.
            "GROUP BY CASE " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 50 THEN '50대 이상' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 40 THEN '40대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 30 THEN '30대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 20 THEN '20대' " +
            "    WHEN (YEAR(CURRENT_DATE) - CAST(o.buyer.birthYear AS INTEGER)) >= 10 THEN '10대' " +
            "    ELSE '기타' END")
    List<AgeRatioDto> findAgeRatioBySeller(@Param("seller") Member seller);
    // =======================================================

    // ★★★ 특정 판매자의 총 판매 건수를 조회하는 메서드 추가 ★★★
    @Query("SELECT COUNT(o) FROM Order o WHERE o.product.seller = :seller")
    long countByProductSeller(@Param("seller") Member seller);

    /**
     * [수정] 가장 많이 팔린 상품 ID 목록 조회 (비로그인 사용자용)
     */
    @Query("SELECT o.product.id FROM Order o GROUP BY o.product.id ORDER BY COUNT(o.product.id) DESC")
    List<Long> findTopSellingProductIds(Pageable pageable);

    /**
     * [추가] 특정 상품들을 제외하고 가장 많이 팔린 상품 ID 목록 조회 (로그인 사용자 추천 채우기용)
     */
    @Query("SELECT o.product.id FROM Order o " +
            "WHERE o.product.id NOT IN :excludedIds " +
            "GROUP BY o.product.id ORDER BY COUNT(o.product.id) DESC")
    List<Long> findTopSellingProductIds(@Param("excludedIds") List<Long> excludedIds, Pageable pageable);

    List<Order> findByBuyer(Member member);

    /**
     * member의 구매내역을 page로 리턴하는 쿼리문을 만들었습니다!
     * 주석은 내가 직접단 것 임!!!!
     */
    @Query("SELECT new com.creatorworks.nexus.order.dto.MemberOrderListDto(" +
            "o.id, p.id, p.name, p.imageUrl, p.primaryCategory, p.secondaryCategory, o.orderDate, s.name) " + // [수정] s.name 추가
            "FROM Order o " +
            "JOIN o.product p " +
            "JOIN p.seller s " + // [추가] 상품(p)의 판매자(s)를 조인
            "WHERE o.buyer = :buyer")
    Page<MemberOrderListDto> findOrderListByBuyer(@Param("buyer") Member buyer, Pageable pageable);
}
