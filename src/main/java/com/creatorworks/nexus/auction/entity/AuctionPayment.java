package com.creatorworks.nexus.auction.entity;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auction_payments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction; // 경매 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private Member bidder; // 입찰자

    @Column(nullable = false)
    private Long amount; // 낙찰 금액

    @Column(nullable = false)
    private String impUid; // 아임포트 결제 UID

    @Column(nullable = false)
    private String merchantUid; // 주문 UID

    @Column(length = 20)
    private String cardNumber; // 마스킹된 카드번호 (마지막 4자리)

    @Column(length = 10)
    private String cardType; // 카드 타입 (VISA, MASTER 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status; // 결제 상태

    @Column(nullable = false)
    private LocalDateTime paymentDate; // 결제 일시

    @Column(length = 500)
    private String failureReason; // 실패 사유

    @Builder
    public AuctionPayment(Auction auction, Member bidder, Long amount,
                          String impUid, String merchantUid, String cardNumber,
                          String cardType, PaymentStatus status, LocalDateTime paymentDate) {
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.impUid = impUid;
        this.merchantUid = merchantUid;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    // 결제 성공 처리
    public void success() {
        this.status = PaymentStatus.SUCCESS;
    }

    // 결제 실패 처리
    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    // 결제 취소 처리
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }
}