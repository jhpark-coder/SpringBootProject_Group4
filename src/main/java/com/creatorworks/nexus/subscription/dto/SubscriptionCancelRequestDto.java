package com.creatorworks.nexus.subscription.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionCancelRequestDto {
    
    private String reason; // 구독 취소 사유
} 