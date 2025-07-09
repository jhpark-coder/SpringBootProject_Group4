package com.creatorworks.nexus.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointRefundRequest {
    
    private Long amount; // 환불 요청 포인트 금액
    private String reason; // 환불 사유
    private String bankCode; // 은행 코드 (환불 계좌 정보)
    private String accountNumber; // 계좌번호
    private String accountHolder; // 예금주명
    private String phoneNumber; // 연락처
} 