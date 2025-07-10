package com.creatorworks.nexus.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.order.entity.Refund;
import com.creatorworks.nexus.order.entity.Refund.RefundStatus;
import com.creatorworks.nexus.order.entity.Refund.RefundType;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    /**
     * 특정 회원의 환불 요청 목록을 조회합니다.
     * @param member 회원
     * @param pageable 페이징 정보
     * @return 환불 요청 목록 페이지
     */
    Page<Refund> findByMemberOrderByRegTimeDesc(Member member, Pageable pageable);
    
    /**
     * 특정 회원의 환불 요청 목록을 조회합니다.
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 환불 요청 목록 페이지
     */
    Page<Refund> findByMemberIdOrderByRegTimeDesc(Long memberId, Pageable pageable);
    
    /**
     * 특정 상태의 환불 요청 목록을 조회합니다.
     * @param status 환불 상태
     * @param pageable 페이징 정보
     * @return 환불 요청 목록 페이지
     */
    Page<Refund> findByRefundStatusOrderByRegTimeDesc(RefundStatus status, Pageable pageable);
    
    /**
     * 특정 타입의 환불 요청 목록을 조회합니다.
     * @param refundType 환불 타입
     * @param pageable 페이징 정보
     * @return 환불 요청 목록 페이지
     */
    Page<Refund> findByRefundTypeOrderByRegTimeDesc(RefundType refundType, Pageable pageable);
    
    /**
     * 특정 회원의 특정 상태 환불 요청이 있는지 확인합니다.
     * @param memberId 회원 ID
     * @param status 환불 상태
     * @return 해당 상태의 환불 요청 존재 여부
     */
    boolean existsByMemberIdAndRefundStatus(Long memberId, RefundStatus status);
    
    /**
     * 특정 회원의 특정 상태들의 환불 요청이 있는지 확인합니다.
     * @param memberId 회원 ID
     * @param statuses 환불 상태 목록
     * @return 해당 상태의 환불 요청 존재 여부
     */
    boolean existsByMemberIdAndRefundStatusIn(Long memberId, List<RefundStatus> statuses);
    
    /**
     * 특정 환불 UID로 환불 요청을 조회합니다.
     * @param refundUid 환불 UID
     * @return 환불 요청
     */
    Optional<Refund> findByRefundUid(String refundUid);
    
    /**
     * 특정 원본 결제 UID로 환불 요청을 조회합니다.
     * @param originalImpUid 원본 결제 UID
     * @return 환불 요청 목록
     */
    List<Refund> findByOriginalImpUid(String originalImpUid);
    
    /**
     * 특정 주문에 대한 환불 요청을 조회합니다.
     * @param orderId 주문 ID
     * @return 환불 요청 목록
     */
    List<Refund> findByOrderId(Long orderId);
    
    /**
     * 특정 결제에 대한 환불 요청을 조회합니다.
     * @param paymentId 결제 ID
     * @return 환불 요청 목록
     */
    List<Refund> findByPaymentId(Long paymentId);
    
    /**
     * 특정 회원의 총 환불 요청 금액을 계산합니다.
     * @param memberId 회원 ID
     * @return 총 환불 요청 금액
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r " +
           "WHERE r.member.id = :memberId AND r.refundStatus IN ('PENDING', 'PROCESSING')")
    Long calculateTotalRefundRequestAmount(@Param("memberId") Long memberId);
    
    /**
     * 특정 회원의 완료된 환불 총액을 계산합니다.
     * @param memberId 회원 ID
     * @return 완료된 환불 총액
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r " +
           "WHERE r.member.id = :memberId AND r.refundStatus = 'COMPLETED'")
    Long calculateTotalCompletedRefundAmount(@Param("memberId") Long memberId);
    
    /**
     * 특정 상태의 환불 요청 개수를 조회합니다.
     * @param status 환불 상태
     * @return 환불 요청 개수
     */
    long countByRefundStatus(RefundStatus status);
    
    /**
     * 특정 타입의 환불 요청 개수를 조회합니다.
     * @param refundType 환불 타입
     * @return 환불 요청 개수
     */
    long countByRefundType(RefundType refundType);
    
    /**
     * 특정 회원의 환불 요청 개수를 조회합니다.
     * @param memberId 회원 ID
     * @return 환불 요청 개수
     */
    long countByMemberId(Long memberId);
    
    /**
     * 특정 회원의 특정 타입 환불 요청 개수를 조회합니다.
     * @param memberId 회원 ID
     * @param refundType 환불 타입
     * @return 환불 요청 개수
     */
    long countByMemberIdAndRefundType(Long memberId, RefundType refundType);
    
    /**
     * 특정 회원의 특정 상태 환불 요청 개수를 조회합니다.
     * @param memberId 회원 ID
     * @param refundStatus 환불 상태
     * @return 환불 요청 개수
     */
    long countByMemberIdAndRefundStatus(Long memberId, RefundStatus refundStatus);
    
    /**
     * 최근 처리된 환불 요청들을 조회합니다 (관리자용).
     * @param pageable 페이징 정보
     * @return 최근 환불 요청 목록
     */
    @Query("SELECT r FROM Refund r ORDER BY r.regTime DESC")
    Page<Refund> findRecentRefunds(Pageable pageable);
    
    /**
     * 특정 기간 동안의 환불 요청을 조회합니다.
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 환불 요청 목록
     */
    @Query("SELECT r FROM Refund r WHERE r.regTime BETWEEN :startDate AND :endDate ORDER BY r.regTime DESC")
    Page<Refund> findByRegTimeBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                     @Param("endDate") java.time.LocalDateTime endDate,
                                     Pageable pageable);
    
    /**
     * 환불 상세 조회 (본인 확인용)
     * @param refundId 환불 ID
     * @param memberId 회원 ID
     * @return 환불 정보
     */
    Refund findByIdAndMemberId(Long refundId, Long memberId);
    
    /**
     * 환불 상태와 타입별 조회
     * @param refundStatus 환불 상태
     * @param refundType 환불 타입
     * @param pageable 페이징 정보
     * @return 환불 목록
     */
    Page<Refund> findByRefundStatusAndRefundTypeOrderByRegTimeDesc(RefundStatus refundStatus, RefundType refundType, Pageable pageable);
} 