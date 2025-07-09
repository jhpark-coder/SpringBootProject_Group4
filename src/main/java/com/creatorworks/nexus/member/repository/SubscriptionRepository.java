package com.creatorworks.nexus.member.repository;

import com.creatorworks.nexus.member.constant.SubscriptionStatus;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.entity.Subscription;
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

    /**
     * 특정 구독자의 활성 구독 목록 조회
     */
    List<Subscription> findBySubscriberAndStatus(Member subscriber, SubscriptionStatus status);

    /**
     * 특정 구독자의 모든 구독 목록 조회 (페이징)
     */
    Page<Subscription> findBySubscriberOrderByRegTimeDesc(Member subscriber, Pageable pageable);

    /**
     * 특정 작가의 구독자 목록 조회
     */
    List<Subscription> findByAuthorAndStatus(Member author, SubscriptionStatus status);

    /**
     * 구독자와 작가로 특정 구독 조회
     */
    Optional<Subscription> findBySubscriberAndAuthorAndStatus(Member subscriber, Member author, SubscriptionStatus status);

    /**
     * 만료일이 임박한 구독 목록 조회 (7일 이내)
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.endDate BETWEEN :now AND :sevenDaysLater")
    List<Subscription> findExpiringSoon(@Param("status") SubscriptionStatus status,
                                        @Param("now") LocalDateTime now,
                                        @Param("sevenDaysLater") LocalDateTime sevenDaysLater);

    /**
     * 만료된 구독 목록 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.endDate < :now")
    List<Subscription> findExpired(@Param("status") SubscriptionStatus status,
                                   @Param("now") LocalDateTime now);

    /**
     * 다음 결제일이 임박한 구독 목록 조회 (3일 이내)
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.nextBillingDate BETWEEN :now AND :threeDaysLater")
    List<Subscription> findNextBillingSoon(@Param("status") SubscriptionStatus status,
                                           @Param("now") LocalDateTime now,
                                           @Param("threeDaysLater") LocalDateTime threeDaysLater);

    /**
     * 특정 아임포트 UID로 구독 조회
     */
    Optional<Subscription> findByImpUid(String impUid);

    /**
     * 특정 주문 UID로 구독 조회
     */
    Optional<Subscription> findByMerchantUid(String merchantUid);

    /**
     * 특정 고객 UID로 구독 조회
     */
    Optional<Subscription> findByCustomerUid(String customerUid);

    /**
     * 구독자 ID로 활성 구독 개수 조회
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.subscriber.id = :subscriberId AND s.status = :status")
    long countActiveSubscriptionsBySubscriberId(@Param("subscriberId") Long subscriberId,
                                                @Param("status") SubscriptionStatus status);

    /**
     * 작가 ID로 구독자 수 조회
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.author.id = :authorId AND s.status = :status")
    long countSubscribersByAuthorId(@Param("authorId") Long authorId,
                                    @Param("status") SubscriptionStatus status);

    /**
     * 특정 상태의 구독 목록 조회
     */
    List<Subscription> findByStatus(SubscriptionStatus status);
}