package com.creatorworks.nexus.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Point;

public interface PointRepository extends JpaRepository<Point, Long> {
    
    /**
     * 특정 회원의 포인트 내역을 조회합니다.
     * @param member 회원
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    Page<Point> findByMemberOrderByRegTimeDesc(Member member, Pageable pageable);
    
    /**
     * 특정 회원의 포인트 내역을 조회합니다.
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 포인트 내역 페이지
     */
    Page<Point> findByMemberIdOrderByRegTimeDesc(Long memberId, Pageable pageable);
    
    /**
     * 특정 회원의 포인트 잔액을 계산합니다.
     * @param memberId 회원 ID
     * @return 포인트 잔액 (최소 0)
     */
    @Query("SELECT GREATEST(COALESCE(SUM(p.amount), 0), 0) FROM Point p WHERE p.member.id = :memberId")
    Long calculateBalanceByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 특정 회원의 포인트 잔액을 계산합니다.
     * @param member 회원
     * @return 포인트 잔액 (최소 0)
     */
    @Query("SELECT GREATEST(COALESCE(SUM(p.amount), 0), 0) FROM Point p WHERE p.member = :member")
    Long calculateBalanceByMember(@Param("member") Member member);
    
    /**
     * 특정 아임포트 UID로 포인트 내역을 조회합니다.
     * @param impUid 아임포트 UID
     * @return 포인트 내역
     */
    Point findByImpUid(String impUid);
    
    /**
     * 특정 회원의 특정 타입 포인트 내역을 조회합니다.
     * @param memberId 회원 ID
     * @param type 포인트 타입
     * @return 포인트 내역 목록
     */
    List<Point> findByMemberIdAndTypeOrderByRegTimeDesc(Long memberId, Point.PointType type);
    
    /**
     * 특정 주문 UID로 포인트 내역을 조회합니다.
     * @param merchantUid 주문 UID
     * @return 포인트 내역
     */
    Point findByMerchantUid(String merchantUid);
} 