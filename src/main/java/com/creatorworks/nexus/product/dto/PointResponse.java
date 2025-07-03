package com.creatorworks.nexus.product.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointResponse {
    
    private boolean success;
    private String message;
    private Long currentBalance; // 현재 포인트 잔액
    private Long amount; // 거래 금액
    private String description; // 거래 설명
    private LocalDateTime transactionDate; // 거래 일시
} 