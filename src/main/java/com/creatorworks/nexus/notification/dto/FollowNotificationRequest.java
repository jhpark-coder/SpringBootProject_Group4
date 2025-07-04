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
public class FollowNotificationRequest { // 팔로우 알림 왔을때 요청 데이터(db로 저장할 데이터)
    private Long targetUserId;   // 알림 받을 사람(팔로우 당한 사람)
    private Long senderUserId;   // 팔로우 건 사람
    private Long followId;       // 팔로우 관계 ID(필요시)
    private String message;      // 알림 메시지
    private String type = "follow"; // "follow"
    private NotificationCategory category;
    private String link; // 알림 클릭 시 이동할 경로

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public Long getSenderUserId() { return senderUserId; }
    public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }

    public Long getFollowId() { return followId; }
    public void setFollowId(Long followId) { this.followId = followId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
} 