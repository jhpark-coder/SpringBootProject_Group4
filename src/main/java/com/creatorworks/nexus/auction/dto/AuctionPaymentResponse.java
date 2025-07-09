package com.creatorworks.nexus.auction.dto;

import com.creatorworks.nexus.auction.entity.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuctionPaymentResponse {
    private Long id;
    private Long auctionId;
    private String auctionTitle;       // 경매 제목
    private String bidderName;         // 입찰자 이름
    private Long amount;               // 낙찰 금액
    private String impUid;             // 아임포트 결제 UID
    private String merchantUid;        // 주문 UID
    private String cardNumber;         // 마스킹된 카드번호
    private String cardType;           // 카드 타입
    private PaymentStatus status;      // 결제 상태
    private LocalDateTime paymentDate; // 결제 일시
    private String failureReason;      // 실패 사유
}