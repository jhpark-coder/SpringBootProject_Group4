package com.creatorworks.nexus.notification.dto;

import com.creatorworks.nexus.notification.entity.NotificationCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotificationRequest {
    private Long targetUserId;   // 결제한 사용자 ID
    private String message;      // 알림 메시지
    private String type;         // 알림 타입 (payment_success, payment_failed, payment_cancelled)
    private NotificationCategory category;
    private String link;         // 알림 클릭 시 이동할 경로
    private Long amount;         // 결제 금액
    private String paymentMethod; // 결제 방법
    private String orderId;      // 주문 ID (선택적)
} 