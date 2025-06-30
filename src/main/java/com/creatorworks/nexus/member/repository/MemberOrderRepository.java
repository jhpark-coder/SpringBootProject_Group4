package com.creatorworks.nexus.member.repository;

import com.creatorworks.nexus.member.dto.MonthlyCategoryPurchaseDTO;
import com.creatorworks.nexus.member.entity.MemberOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberOrderRepository extends JpaRepository<MemberOrder, Long> {
    @Query("SELECT com.creatorworks.nexus.member.dto.MonthlyCategoryPurchaseDTO(" +
            "    YEAR(o.orderDate), MONTH(o.orderDate), " +
            "    p.category.name, p.category.color, COUNT(o)) " +
            "FROM MemberOrder o JOIN o.product p " +
            "WHERE o.member.id = :memberId AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate), p.category.name, p.category.color " +
            "ORDER BY YEAR(o.orderDate), MONTH(o.orderDate), p.category.name")
    List<MonthlyCategoryPurchaseDTO> findMonthlyCategoryPurchases(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
