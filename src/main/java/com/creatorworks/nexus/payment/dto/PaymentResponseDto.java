package com.creatorworks.nexus.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponseDto {
    
    private Long paymentId;
    private Long productId;
    private String productName;
    private Integer amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentDate;
    private String description;
    private Integer remainingBalance; // 포인트 결제 시 잔액
} 