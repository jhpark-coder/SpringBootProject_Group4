package com.creatorworks.nexus.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundStatisticsDto {
    
    private Long totalRefunds; // 총 환불 요청 수
    
    private Long pendingRefunds; // 대기중인 환불 요청 수
    
    private Long processingRefunds; // 처리중인 환불 요청 수
    
    private Long completedRefunds; // 완료된 환불 요청 수
    
    private Long failedRefunds; // 실패한 환불 요청 수
    
    private Long cancelledRefunds; // 취소된 환불 요청 수
    
    private Long totalRefundAmount; // 총 환불 요청 금액
    
    private Long totalCompletedAmount; // 총 완료된 환불 금액
    
    private Long totalPendingAmount; // 총 대기중인 환불 금액
    
    // 타입별 통계
    private Long pointRefunds; // 포인트 환불 수
    
    private Long paymentRefunds; // 결제 환불 수
    
    private Long subscriptionCancels; // 구독 취소 수
    
    // 금액별 통계
    private Long averageRefundAmount; // 평균 환불 금액
    
    private Long maxRefundAmount; // 최대 환불 금액
    
    private Long minRefundAmount; // 최소 환불 금액
} 