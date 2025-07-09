package com.creatorworks.nexus.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.PointRefund;
import com.creatorworks.nexus.product.entity.PointRefund.RefundStatus;

public interface PointRefundRepository extends JpaRepository<PointRefund, Long> {
    
    /**
     * 특정 회원의 환불 요청 목록을 조회합니다.
     * @param member 회원
     * @param pageable 페이징 정보
     * @return 환불 요청 목록 페이지
     */
    Page<PointRefund> findByMemberOrderByRegTimeDesc(Member member, Pageable pageable);
    
    /**
     * 특정 회원의 환불 요청 목록을 조회합니다.
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 환불 요청 목록 페이지
     */
    Page<PointRefund> findByMemberIdOrderByRegTimeDesc(Long memberId, Pageable pageable);
    
    /**
     * 특정 상태의 환불 요청 목록을 조회합니다.
     * @param status 환불 상태
     * @param pageable 페이징 정보
     * @return 환불 요청 목록 페이지
     */
    Page<PointRefund> findByStatusOrderByRegTimeDesc(RefundStatus status, Pageable pageable);
    
    /**
     * 특정 회원의 대기중인 환불 요청이 있는지 확인합니다.
     * @param memberId 회원 ID
     * @return 대기중인 환불 요청 존재 여부
     */
    boolean existsByMemberIdAndStatus(Long memberId, RefundStatus status);
    
    /**
     * 특정 환불 UID로 환불 요청을 조회합니다.
     * @param refundUid 환불 UID
     * @return 환불 요청
     */
    Optional<PointRefund> findByRefundUid(String refundUid);
    
    /**
     * 특정 회원의 총 환불 요청 금액을 계산합니다.
     * @param memberId 회원 ID
     * @return 총 환불 요청 금액
     */
    @Query("SELECT COALESCE(SUM(pr.amount), 0) FROM PointRefund pr WHERE pr.member.id = :memberId AND pr.status IN ('PENDING', 'APPROVED')")
    Long calculateTotalRefundRequestAmount(@Param("memberId") Long memberId);
    
    /**
     * 특정 상태의 환불 요청 개수를 조회합니다.
     * @param status 환불 상태
     * @return 환불 요청 개수
     */
    long countByStatus(RefundStatus status);
} 