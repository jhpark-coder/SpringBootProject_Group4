package com.creatorworks.nexus.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    
    private Long amount; // 환불 요청 금액
    
    private String reason; // 환불 사유
    
    private String bankCode; // 은행 코드 (환불 계좌 정보)
    
    private String accountNumber; // 계좌번호
    
    private String accountHolder; // 예금주명
    
    private String phoneNumber; // 연락처
    
    // 선택적 필드들 (특정 주문/결제에 대한 환불인 경우)
    private Long orderId; // 환불 대상 주문 ID (선택적)
    
    private Long paymentId; // 환불 대상 결제 ID (선택적)
    
    private String originalImpUid; // 원본 결제 아임포트 UID (선택적)
} 