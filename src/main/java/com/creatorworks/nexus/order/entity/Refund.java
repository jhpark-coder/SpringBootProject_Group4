package com.creatorworks.nexus.order.entity;

import java.time.LocalDateTime;

import com.creatorworks.nexus.global.BaseEntity;
import com.creatorworks.nexus.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 환불 대상 주문 (선택적)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment; // 환불 대상 결제 (선택적)

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false)
    private RefundType refundType; // POINT_REFUND, PAYMENT_REFUND, SUBSCRIPTION_CANCEL

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false)
    private RefundStatus refundStatus; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED

    @Column(nullable = false)
    private Long amount; // 환불 요청 금액

    @Column(length = 500)
    private String reason; // 환불 사유

    @Column(length = 10)
    private String bankCode; // 은행 코드 (환불 계좌 정보)

    @Column(length = 50)
    private String accountNumber; // 계좌번호

    @Column(length = 50)
    private String accountHolder; // 예금주명

    @Column(length = 20)
    private String phoneNumber; // 연락처

    @Column(length = 500)
    private String adminComment; // 관리자 코멘트

    @Column(length = 50)
    private String refundUid; // 환불 고유 ID (아임포트)

    @Column(length = 50)
    private String originalImpUid; // 원본 결제 아임포트 UID

    @Column(length = 50)
    private String originalMerchantUid; // 원본 결제 주문 UID

    @Column
    private Long originalAmount; // 원본 결제 금액

    @Column(name = "refund_date")
    private LocalDateTime refundDate; // 실제 환불 처리 일시

    @Column(length = 500)
    private String failureReason; // 환불 실패 사유

    @Builder
    public Refund(Member member, Order order, Payment payment, RefundType refundType, 
                  RefundStatus refundStatus, Long amount, String reason, String bankCode,
                  String accountNumber, String accountHolder, String phoneNumber,
                  String adminComment, String refundUid, String originalImpUid,
                  String originalMerchantUid, Long originalAmount, LocalDateTime refundDate,
                  String failureReason) {
        this.member = member;
        this.order = order;
        this.payment = payment;
        this.refundType = refundType;
        this.refundStatus = refundStatus;
        this.amount = amount;
        this.reason = reason;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.phoneNumber = phoneNumber;
        this.adminComment = adminComment;
        this.refundUid = refundUid;
        this.originalImpUid = originalImpUid;
        this.originalMerchantUid = originalMerchantUid;
        this.originalAmount = originalAmount;
        this.refundDate = refundDate;
        this.failureReason = failureReason;
    }

    // 환불 상태 변경 메서드들
    public void startProcessing() {
        this.refundStatus = RefundStatus.PROCESSING;
    }

    public void complete() {
        this.refundStatus = RefundStatus.COMPLETED;
        this.refundDate = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.refundStatus = RefundStatus.FAILED;
        this.failureReason = reason;
    }

    public void cancel() {
        this.refundStatus = RefundStatus.CANCELLED;
    }

    public void setAdminComment(String comment) {
        this.adminComment = comment;
    }

    // 컨트롤러에서 사용하는 메서드들
    public String getStatus() {
        return this.refundStatus.name();
    }

    public LocalDateTime getRequestDate() {
        return this.getRegTime();
    }

    public LocalDateTime getProcessedDate() {
        return this.refundDate;
    }

    public enum RefundType {
        POINT_REFUND("포인트 환불"),
        PAYMENT_REFUND("결제 환불"),
        SUBSCRIPTION_CANCEL("구독 취소");

        private final String description;

        RefundType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RefundStatus {
        PENDING("대기중"),
        PROCESSING("처리중"),
        COMPLETED("완료됨"),
        FAILED("실패"),
        CANCELLED("취소됨");

        private final String description;

        RefundStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 