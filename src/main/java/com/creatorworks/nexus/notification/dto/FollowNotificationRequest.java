package com.creatorworks.nexus.notification.dto;

public class FollowNotificationRequest {
    private Long targetUserId;   // 알림 받을 사람(팔로우 당한 사람)
    private Long senderUserId;   // 팔로우 건 사람
    private Long followId;       // 팔로우 관계 ID(필요시)
    private String message;      // 알림 메시지
    private String type = "follow"; // "follow"

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