package com.creatorworks.nexus.member.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.constant.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Member subscriber; // 구독자
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author; // 작가
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; // 구독 상태
    
    @Column(nullable = false)
    private Integer months; // 구독 개월 수
    
    @Column(nullable = false)
    private Long amount; // 월 결제 금액
    
    @Column(nullable = false)
    private LocalDateTime startDate; // 구독 시작일
    
    @Column(nullable = false)
    private LocalDateTime endDate; // 구독 만료일
    
    @Column(nullable = false)
    private LocalDateTime nextBillingDate; // 다음 결제일
    
    @Column(length = 50)
    private String impUid; // 아임포트 결제 UID
    
    @Column(length = 50)
    private String merchantUid; // 주문 UID
    
    @Column(length = 20)
    private String cardNumber; // 마스킹된 카드번호 (마지막 4자리)
    
    @Column(length = 10)
    private String cardType; // 카드 타입 (VISA, MASTER 등)
    
    @Column(length = 20)
    private String customerUid; // 아임포트 고객 UID (정기결제용)
    
    @Builder
    public Subscription(Member subscriber, Member author, SubscriptionStatus status, 
                       Integer months, Long amount, LocalDateTime startDate, 
                       LocalDateTime endDate, LocalDateTime nextBillingDate,
                       String impUid, String merchantUid, String cardNumber, 
                       String cardType, String customerUid) {
        this.subscriber = subscriber;
        this.author = author;
        this.status = status;
        this.months = months;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextBillingDate = nextBillingDate;
        this.impUid = impUid;
        this.merchantUid = merchantUid;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.customerUid = customerUid;
    }
    
    // 구독 활성화
    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
    }
    
    // 구독 비활성화
    public void deactivate() {
        this.status = SubscriptionStatus.INACTIVE;
    }
    
    // 구독 연장
    public void extend(Integer additionalMonths) {
        this.endDate = this.endDate.plusMonths(additionalMonths);
        this.nextBillingDate = this.nextBillingDate.plusMonths(additionalMonths);
        this.months += additionalMonths;
    }
    
    // 만료일까지 남은 일수 계산
    public long getDaysUntilExpiration() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), this.endDate);
    }
    
    // 다음 결제일까지 남은 일수 계산
    public long getDaysUntilNextBilling() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), this.nextBillingDate);
    }
} 