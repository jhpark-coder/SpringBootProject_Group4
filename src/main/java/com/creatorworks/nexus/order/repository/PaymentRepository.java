package com.creatorworks.nexus.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.entity.Payment.PaymentStatus;
import com.creatorworks.nexus.order.entity.Payment.PaymentType;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * impUid로 결제 정보를 조회합니다.
     */
    Optional<Payment> findByImpUid(String impUid);
    
    /**
     * merchantUid로 결제 정보를 조회합니다.
     */
    Optional<Payment> findByMerchantUid(String merchantUid);
    
    /**
     * customerUid로 정기결제 정보를 조회합니다.
     */
    Optional<Payment> findByCustomerUid(String customerUid);
    
    /**
     * 결제 상태별 결제 목록을 조회합니다.
     */
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
    
    /**
     * 결제 타입별 결제 목록을 조회합니다.
     */
    List<Payment> findByPaymentType(PaymentType paymentType);
    
    /**
     * 특정 기간 동안의 결제 목록을 조회합니다.
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * 다음 결제일이 임박한 정기결제를 조회합니다.
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'COMPLETED' " +
           "AND p.nextBillingDate BETWEEN :now AND :threeDaysLater")
    List<Payment> findUpcomingRecurringPayments(@Param("now") LocalDateTime now,
                                               @Param("threeDaysLater") LocalDateTime threeDaysLater);
    
    /**
     * 결제 실패한 정기결제를 조회합니다.
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'FAILED' " +
           "AND p.customerUid IS NOT NULL")
    List<Payment> findFailedRecurringPayments();
    
    /**
     * 중복 결제 방지를 위한 impUid 존재 여부 확인
     */
    boolean existsByImpUid(String impUid);
    
    /**
     * 중복 주문 방지를 위한 merchantUid 존재 여부 확인
     */
    boolean existsByMerchantUid(String merchantUid);
} 