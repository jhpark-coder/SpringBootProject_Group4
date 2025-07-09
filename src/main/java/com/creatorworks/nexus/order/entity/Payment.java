package com.creatorworks.nexus.order.entity;

import java.time.LocalDateTime;

import com.creatorworks.nexus.global.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType; // POINT, CARD, BANK_TRANSFER

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus; // PENDING, COMPLETED, FAILED, CANCELLED

    @Column(nullable = false)
    private Long amount; // 결제 금액

    @Column(length = 50)
    private String impUid; // 아임포트 결제 UID

    @Column(length = 50)
    private String merchantUid; // 주문 UID

    @Column(length = 20)
    private String customerUid; // 아임포트 고객 UID (정기결제용)

    @Column(length = 20)
    private String cardNumber; // 마스킹된 카드번호 (마지막 4자리)

    @Column(length = 10)
    private String cardType; // 카드 타입 (VISA, MASTER 등)

    @Column(name = "payment_date")
    private LocalDateTime paymentDate; // 실제 결제 일시

    @Column(length = 500)
    private String failureReason; // 결제 실패 사유

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate; // 다음 결제일 (정기결제용)

    @Builder
    public Payment(Order order, PaymentType paymentType, PaymentStatus paymentStatus, 
                  Long amount, String impUid, String merchantUid, String customerUid,
                  String cardNumber, String cardType, LocalDateTime paymentDate,
                  String failureReason, LocalDateTime nextBillingDate) {
        this.order = order;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
        this.amount = amount;
        this.impUid = impUid;
        this.merchantUid = merchantUid;
        this.customerUid = customerUid;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.paymentDate = paymentDate;
        this.failureReason = failureReason;
        this.nextBillingDate = nextBillingDate;
    }

    // 결제 상태 변경 메서드들
    public void complete() {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.paymentDate = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.paymentStatus = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void cancel() {
        this.paymentStatus = PaymentStatus.CANCELLED;
    }

    // 정기결제용 다음 결제일 설정
    public void setNextBillingDate(LocalDateTime nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }

    public enum PaymentType {
        POINT("포인트"),
        CARD("카드"),
        BANK_TRANSFER("계좌이체");

        private final String description;

        PaymentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum PaymentStatus {
        PENDING("대기중"),
        COMPLETED("완료"),
        FAILED("실패"),
        CANCELLED("취소");

        private final String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 