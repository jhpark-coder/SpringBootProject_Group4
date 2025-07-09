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
            "    p.primaryCategory, COUNT(o)) " +
            "FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
            "WHERE o.buyer.id = :memberId AND o.regTime BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(o.regTime), MONTH(o.regTime), p.primaryCategory " +
            "ORDER BY YEAR(o.regTime), MONTH(o.regTime), p.primaryCategory")
    List<MonthlyCategoryPurchaseDTO> findMonthlyCategoryPurchases(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}