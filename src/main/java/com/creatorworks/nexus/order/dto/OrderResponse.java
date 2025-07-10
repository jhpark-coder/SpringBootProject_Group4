package com.creatorworks.nexus.order.dto;

import java.time.LocalDateTime;

import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Order.OrderStatus;
import com.creatorworks.nexus.order.entity.Order.OrderType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderResponse {
    private Long orderId;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private Long totalAmount;
    private LocalDateTime orderDate;
    private String description;
    private ProductResponse product;
    private PaymentResponse payment;
    
    @Getter
    @Setter
    @Builder
    public static class ProductResponse {
        private Long productId;
        private String productName;
        private String productImageUrl;
        private String sellerName;
        private Long price;
    }
    
    @Getter
    @Setter
    @Builder
    public static class PaymentResponse {
        private String paymentType;
        private String paymentStatus;
        private Long amount;
        private String impUid;
        private String merchantUid;
        private String cardNumber;
        private String cardType;
        private LocalDateTime paymentDate;
        private LocalDateTime nextBillingDate;
    }
    
    public static OrderResponse from(Order order) {
        ProductResponse productResponse = null;
        if (order.getProduct() != null) {
            productResponse = ProductResponse.builder()
                    .productId(order.getProduct().getId())
                    .productName(order.getProduct().getName())
                    .productImageUrl(order.getProduct().getImageUrl())
                    .sellerName(order.getProduct().getSeller().getName())
                    .price(order.getProduct().getPrice())
                    .build();
        }
        
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .description(order.getDescription())
                .product(productResponse)
                .build();
    }
} 