package com.creatorworks.nexus.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {
    
    private Long productId;
    private Integer amount;
    private String paymentMethod; // POINT, CARD, BANK_TRANSFER
    private String description;
} 