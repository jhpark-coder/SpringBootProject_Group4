package com.creatorworks.nexus.subscription.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionRequestDto {
    
    private Long productId;
    private Long authorId;
    private Integer monthlyPrice;
    private String paymentMethod; // POINT, CARD, BANK_TRANSFER
    private Boolean autoRenewal = true;
} 