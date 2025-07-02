package com.creatorworks.nexus.payment.repository;

import com.creatorworks.nexus.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Page<Payment> findByMemberIdOrderByRegTimeDesc(Long memberId, Pageable pageable);
    
    List<Payment> findByMemberIdAndProductId(Long memberId, Long productId);
    
    List<Payment> findByMemberIdAndStatus(Long memberId, Payment.PaymentStatus status);
} 