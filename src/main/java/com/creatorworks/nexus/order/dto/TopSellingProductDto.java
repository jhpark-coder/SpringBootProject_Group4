package com.creatorworks.nexus.order.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSellingProductDto {

    private Long id;
    private String name;
    private String imageUrl;
    private String sellerName; // 판매자 이름
    private Long salesCount; // 판매량을 담을 필드
}