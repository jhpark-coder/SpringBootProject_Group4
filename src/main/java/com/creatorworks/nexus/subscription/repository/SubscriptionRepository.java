package com.creatorworks.nexus.subscription.repository;

import com.creatorworks.nexus.subscription.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    // 구독자의 모든 구독 조회
    Page<Subscription> findBySubscriberIdOrderByRegTimeDesc(Long subscriberId, Pageable pageable);
    
    // 작가의 모든 구독자 조회
    Page<Subscription> findByAuthorIdOrderByRegTimeDesc(Long authorId, Pageable pageable);
    
    // 특정 구독자와 작가의 구독 조회
    Optional<Subscription> findBySubscriberIdAndAuthorId(Long subscriberId, Long authorId);
    
    // 활성 구독 조회
    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);
    
    // 자동 갱신이 활성화된 구독 조회
    List<Subscription> findByAutoRenewalTrueAndStatus(Subscription.SubscriptionStatus status);
    
    // 다음 결제일이 도래한 구독 조회 (크론잡용)
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate <= :now AND s.autoRenewal = true AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsForRenewal(@Param("now") LocalDateTime now);
    
    // 만료된 구독 조회 (크론잡용)
    @Query("SELECT s FROM Subscription s WHERE s.endDate <= :now AND s.status = 'ACTIVE'")
    List<Subscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
} 