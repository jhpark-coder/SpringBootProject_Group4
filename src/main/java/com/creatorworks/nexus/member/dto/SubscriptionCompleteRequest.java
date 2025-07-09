package com.creatorworks.nexus.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCompleteRequest {

    private String plan; // monthly, yearly
    private Long amount; // 월 결제 금액
    private String authorName; // 작가명
    private Long productId; // 상품 ID
    private String impUid; // 아임포트 UID
    private String merchantUid; // 주문 UID
    private String customerUid; // 고객 UID (정기결제용)
    private String cardNumber; // 마스킹된 카드번호
    private String cardType; // 카드 타입
    private Integer months; // 구독 개월 수
}