package com.creatorworks.nexus.order.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberOrderListDto {
    private Long orderId;
    private Long productId;
    private String productName;
    private String seller;
    private String imageUrl;
    private String primaryCategory;
    private String secondaryCategory;
    private LocalDateTime orderDate;

    // JPQL의 new 오퍼레이터를 사용하기 위해 모든 필드를 받는 생성자가 필요합니다.
    public MemberOrderListDto(Long orderId, Long productId, String productName, String imageUrl,
                              String primaryCategory, String secondaryCategory, LocalDateTime orderDate, String seller) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.seller = seller;
        this.imageUrl = imageUrl;
        this.primaryCategory = primaryCategory;
        this.secondaryCategory = secondaryCategory;
        this.orderDate = orderDate;
    }
}
