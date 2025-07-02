package com.creatorworks.nexus.subscription.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id")
    private Member subscriber; // 구독자
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author; // 작가
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 구독 시작한 상품
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; // ACTIVE, EXPIRED, CANCELLED
    
    @Column(nullable = false)
    private Integer monthlyPrice; // 월 구독료
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate; // 구독 시작일
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate; // 구독 종료일
    
    @Column(name = "next_billing_date", nullable = false)
    private LocalDateTime nextBillingDate; // 다음 결제일
    
    @Column(name = "auto_renewal", nullable = false)
    private Boolean autoRenewal = true; // 자동 갱신 여부
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // 구독 취소일
    
    @Column(length = 500)
    private String cancelReason; // 구독 취소 사유
    
    public enum SubscriptionStatus {
        ACTIVE,     // 활성
        EXPIRED,    // 만료
        CANCELLED   // 취소
    }
    
    /**
     * 구독이 활성 상태인지 확인
     */
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && 
               LocalDateTime.now().isBefore(endDate);
    }
    
    /**
     * 구독 취소
     */
    public void cancel(String reason) {
        this.status = SubscriptionStatus.CANCELLED;
        this.autoRenewal = false;
        this.cancelledAt = LocalDateTime.now();
        this.cancelReason = reason;
    }
    
    /**
     * 구독 갱신
     */
    public void renew() {
        this.endDate = this.endDate.plusMonths(1);
        this.nextBillingDate = this.nextBillingDate.plusMonths(1);
    }
} 