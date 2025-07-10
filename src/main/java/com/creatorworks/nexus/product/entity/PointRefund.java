package com.creatorworks.nexus.product.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointRefund extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(nullable = false)
    private Long amount; // 환불 요청 포인트 금액
    
    @Column(length = 500)
    private String reason; // 환불 사유
    
    @Column(length = 10)
    private String bankCode; // 은행 코드
    
    @Column(length = 50)
    private String accountNumber; // 계좌번호
    
    @Column(length = 50)
    private String accountHolder; // 예금주명
    
    @Column(length = 20)
    private String phoneNumber; // 연락처
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status; // 환불 상태
    
    @Column(length = 500)
    private String adminComment; // 관리자 코멘트
    
    @Column(length = 50)
    private String refundUid; // 환불 고유 ID
    
    @Builder
    public PointRefund(Member member, Long amount, String reason, String bankCode, 
                      String accountNumber, String accountHolder, String phoneNumber, 
                      RefundStatus status, String adminComment, String refundUid) {
        this.member = member;
        this.amount = amount;
        this.reason = reason;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.adminComment = adminComment;
        this.refundUid = refundUid;
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