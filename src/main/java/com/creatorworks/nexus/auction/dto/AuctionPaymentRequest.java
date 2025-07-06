package com.creatorworks.nexus.auction.dto;

import lombok.Data;

@Data
public class AuctionPaymentRequest {
    private Long auctionId;        // 경매 ID
    private Long amount;           // 낙찰 금액
    private String impUid;         // 아임포트 결제 UID
    private String merchantUid;    // 주문 UID
    private String cardNumber;     // 마스킹된 카드번호
    private String cardType;       // 카드 타입
} 