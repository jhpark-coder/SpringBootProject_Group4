package com.creatorworks.nexus.faq.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FaqCategory {
    MEMBERSHIP("회원가입/로그인"),
    PURCHASE("작품 구매 및 이용"),
    PAYMENT("결제/환불"),
    SUBSCRIPTION("구독 서비스"),
    AUCTION("경매"),
    ACCOUNT("계정 및 정보 관리"),
    ETC("기타");
    
    private final String displayName;
    
    FaqCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getValue() {
        return this.name();
    }
    
    @JsonValue
    public String toJson() {
        return this.displayName;
    }
    
    @JsonCreator
    public static FaqCategory fromDisplayName(String displayName) {
        for (FaqCategory category : values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        return null;
    }
} 