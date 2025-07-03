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
public class PointChargeRequest {
    
    private Long amount; // 충전할 포인트 금액
    private Long paymentAmount; // 실제 결제 금액
    private String impUid; // 아임포트 결제 UID
    private String merchantUid; // 주문 UID
} 