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
public class ReviewNotificationRequest {
    private Long targetUserId;   // 알림 받을 사람(판매자)
    private Long senderUserId;   // 후기 작성한 사람(구매자)
    private Long productId;      // 상품 ID
    private String message;      // 알림 메시지
    private String type = "review"; // "review"
    private NotificationCategory category;
    private String link; // 알림 클릭 시 이동할 경로
    private int rating; // 평점

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public NotificationCategory getCategory() { return category; }
    public void setCategory(NotificationCategory category) { this.category = category; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
} 