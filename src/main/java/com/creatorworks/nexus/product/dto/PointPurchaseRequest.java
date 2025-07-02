package com.creatorworks.nexus.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointPurchaseRequest {
    private Long productId;
    private Integer price;
} 