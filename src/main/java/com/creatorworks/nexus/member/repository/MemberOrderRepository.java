package com.creatorworks.nexus.member.repository;

import com.creatorworks.nexus.member.dto.MonthlyCategoryPurchaseDTO;
import com.creatorworks.nexus.member.entity.MemberOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberOrderRepository extends JpaRepository<MemberOrder, Long> {
    // MemberOrderRepository.java
    // MemberOrderRepository.java
    @Query("SELECT new com.creatorworks.nexus.member.dto.MonthlyCategoryPurchaseDTO(" +
            "    YEAR(o.orderDate), MONTH(o.orderDate), " +
            "    p.primaryCategory, COUNT(o)) " + // secondaryCategory 조회 제거
            "FROM Order o JOIN o.product p " +
            "WHERE o.buyer.id = :memberId AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate), p.primaryCategory " + // secondaryCategory 그룹핑 제거
            "ORDER BY YEAR(o.orderDate), MONTH(o.orderDate), p.primaryCategory")
    List<MonthlyCategoryPurchaseDTO> findMonthlyCategoryPurchases(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}