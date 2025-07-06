package com.creatorworks.nexus.member.constant;

public enum SubscriptionStatus {
    ACTIVE("활성"),      // 활성 구독
    INACTIVE("비활성"),   // 비활성 구독 (만료됨)
    PENDING("대기"),     // 결제 대기
    CANCELLED("취소");   // 구독 취소
    
    private final String description;
    
    SubscriptionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 