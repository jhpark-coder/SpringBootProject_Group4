package com.creatorworks.nexus.admin.repository;

import com.creatorworks.nexus.admin.entity.SellerRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SellerRequestRepository extends JpaRepository<SellerRequest, Long> {

    // 대기중인 신청 목록 조회
    List<SellerRequest> findByStatusOrderByRegTimeDesc(SellerRequest.RequestStatus status);

    // 특정 회원의 신청 조회
    Optional<SellerRequest> findByMemberIdAndStatus(Long memberId, SellerRequest.RequestStatus status);

    // 특정 회원의 모든 신청 조회
    List<SellerRequest> findByMemberIdOrderByRegTimeDesc(Long memberId);

    // 대기중인 신청이 있는지 확인
    boolean existsByMemberIdAndStatus(Long memberId, SellerRequest.RequestStatus status);
} 