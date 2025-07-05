package com.creatorworks.nexus.product.dto;

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
public class PointPurchaseRequest {
    
    private Long productId; // 구매할 상품 ID
    private Long price; // 상품 가격 (포인트)
} 