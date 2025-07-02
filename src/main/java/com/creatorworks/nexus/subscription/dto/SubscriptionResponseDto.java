package com.creatorworks.nexus.subscription.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SubscriptionResponseDto {
    
    private Long subscriptionId;
    private Long productId;
    private String productName;
    private Long authorId;
    private String authorName;
    private String status;
    private Integer monthlyPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextBillingDate;
    private Boolean autoRenewal;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private Integer remainingBalance; // 포인트 결제 시 잔액
} 