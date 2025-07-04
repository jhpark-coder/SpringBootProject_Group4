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
public class SellerRequestNotificationRequest {
    private Long targetUserId;   // 신청자 ID
    private String message;      // 알림 메시지
    private String type;         // 알림 타입 (seller_approved, seller_rejected)
    private String reason;       // 거절 사유 (거절 시에만)
    private NotificationCategory category;
    private String link; // 알림 클릭 시 이동할 경로
} 