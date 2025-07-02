package com.creatorworks.nexus.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionRequest {
    private Long productId;
    private Long authorId;
} 