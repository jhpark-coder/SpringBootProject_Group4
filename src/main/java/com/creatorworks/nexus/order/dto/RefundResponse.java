package com.creatorworks.nexus.order.dto;

import java.time.LocalDateTime;

import com.creatorworks.nexus.order.entity.Refund;
import com.creatorworks.nexus.order.entity.Refund.RefundStatus;
import com.creatorworks.nexus.order.entity.Refund.RefundType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    
    private boolean success; // 성공 여부
    
    private String message; // 응답 메시지
    
    private Long refundId; // 환불 ID (컨트롤러용)
    
    private String status; // 상태 (컨트롤러용)
    
    private LocalDateTime requestDate; // 요청일 (컨트롤러용)
    
    private LocalDateTime processedDate; // 처리일 (컨트롤러용)
    
    private Long id; // 환불 ID
    
    private Long amount; // 환불 금액
    
    private String reason; // 환불 사유
    
    private RefundType refundType; // 환불 타입
    
    private RefundStatus refundStatus; // 환불 상태
    
    private String statusDescription; // 상태 설명
    
    private String refundUid; // 환불 고유 ID (아임포트)
    
    private String adminComment; // 관리자 코멘트
    
    private String failureReason; // 실패 사유
    
    private LocalDateTime refundDate; // 실제 환불 처리 일시
    
    private LocalDateTime createdAt; // 생성일
    
    private LocalDateTime updatedAt; // 수정일
    
    // 계좌 정보 (마스킹 처리)
    private String bankCode; // 은행 코드
    
    private String accountNumber; // 계좌번호 (마스킹)
    
    private String accountHolder; // 예금주명
    
    private String phoneNumber; // 연락처 (마스킹)
    
    // 원본 결제 정보
    private String originalImpUid; // 원본 결제 아임포트 UID
    
    private String originalMerchantUid; // 원본 결제 주문 UID
    
    private Long originalAmount; // 원본 결제 금액
    
    // 연관 정보 (선택적)
    private OrderResponse order; // 환불 대상 주문
    
    private PaymentResponse payment; // 환불 대상 결제
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        private Long orderId;
        private String orderType;
        private String orderStatus;
        private Long totalAmount;
        private LocalDateTime orderDate;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private Long paymentId;
        private String paymentType;
        private String paymentStatus;
        private Long amount;
        private String impUid;
        private String merchantUid;
        private LocalDateTime paymentDate;
    }
    
    public static RefundResponse from(Refund refund) {
        // 계좌번호 마스킹 처리
        String maskedAccountNumber = refund.getAccountNumber();
        if (maskedAccountNumber != null && maskedAccountNumber.length() > 4) {
            maskedAccountNumber = maskedAccountNumber.substring(0, 2) + 
                                "****" + 
                                maskedAccountNumber.substring(maskedAccountNumber.length() - 2);
        }
        
        // 연락처 마스킹 처리
        String maskedPhoneNumber = refund.getPhoneNumber();
        if (maskedPhoneNumber != null && maskedPhoneNumber.length() > 4) {
            maskedPhoneNumber = maskedPhoneNumber.substring(0, 3) + 
                              "****" + 
                              maskedPhoneNumber.substring(maskedPhoneNumber.length() - 4);
        }
        
        // 연관 주문 정보
        OrderResponse orderResponse = null;
        if (refund.getOrder() != null) {
            orderResponse = OrderResponse.builder()
                    .orderId(refund.getOrder().getId())
                    .orderType(refund.getOrder().getOrderType().name())
                    .orderStatus(refund.getOrder().getOrderStatus().name())
                    .totalAmount(refund.getOrder().getTotalAmount())
                    .orderDate(refund.getOrder().getOrderDate())
                    .description(refund.getOrder().getDescription())
                    .build();
        }
        
        // 연관 결제 정보
        PaymentResponse paymentResponse = null;
        if (refund.getPayment() != null) {
            paymentResponse = PaymentResponse.builder()
                    .paymentId(refund.getPayment().getId())
                    .paymentType(refund.getPayment().getPaymentType().name())
                    .paymentStatus(refund.getPayment().getPaymentStatus().name())
                    .amount(refund.getPayment().getAmount())
                    .impUid(refund.getPayment().getImpUid())
                    .merchantUid(refund.getPayment().getMerchantUid())
                    .paymentDate(refund.getPayment().getPaymentDate())
                    .build();
        }
        
        return RefundResponse.builder()
                .id(refund.getId())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .refundType(refund.getRefundType())
                .refundStatus(refund.getRefundStatus())
                .statusDescription(refund.getRefundStatus().getDescription())
                .refundUid(refund.getRefundUid())
                .adminComment(refund.getAdminComment())
                .failureReason(refund.getFailureReason())
                .refundDate(refund.getRefundDate())
                .createdAt(refund.getRegTime())
                .updatedAt(refund.getUpdateTime())
                .bankCode(refund.getBankCode())
                .accountNumber(maskedAccountNumber)
                .accountHolder(refund.getAccountHolder())
                .phoneNumber(maskedPhoneNumber)
                .originalImpUid(refund.getOriginalImpUid())
                .originalMerchantUid(refund.getOriginalMerchantUid())
                .originalAmount(refund.getOriginalAmount())
                .order(orderResponse)
                .payment(paymentResponse)
                .build();
    }
} 