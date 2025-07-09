package com.creatorworks.nexus.member.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.dto.MonthlyCategoryPurchaseDTO;
import com.creatorworks.nexus.member.entity.MemberOrder;

public interface MemberOrderRepository extends JpaRepository<MemberOrder, Long> {
    // MemberOrderRepository.java
    // MemberOrderRepository.java
    @Query("SELECT new com.creatorworks.nexus.member.dto.MonthlyCategoryPurchaseDTO(" +
            "    YEAR(o.regTime), MONTH(o.regTime), " +
            "    o.product.primaryCategory, COUNT(o)) " +
            "FROM Order o " +
            "WHERE o.buyer.id = :memberId AND o.regTime BETWEEN :startDate AND :endDate " +
            "  AND o.product IS NOT NULL " +
            "GROUP BY YEAR(o.regTime), MONTH(o.regTime), o.product.primaryCategory " +
            "ORDER BY YEAR(o.regTime), MONTH(o.regTime), o.product.primaryCategory")
    List<MonthlyCategoryPurchaseDTO> findMonthlyCategoryPurchases(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}