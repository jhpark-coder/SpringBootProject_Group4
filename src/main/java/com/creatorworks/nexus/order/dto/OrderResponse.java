package com.creatorworks.nexus.order.dto;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<OrderItemResponse> orderItems;
    private PaymentResponse payment;
    
    @Getter
    @Setter
    @Builder
    public static class OrderItemResponse {
        private Long itemId;
        private String itemName;
        private String itemType;
        private Long price;
        private Integer quantity;
        private String description;
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
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderDate())
                .description(order.getDescription())
                .build();
    }
} 