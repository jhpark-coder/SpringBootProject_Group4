package com.creatorworks.nexus.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private Long productId;
    private Integer quantity;
    private Long amount;
    private String impUid;
    private String merchantUid;
    private String customerUid;
    private String cardNumber;
    private String cardType;
    private String description;
} 