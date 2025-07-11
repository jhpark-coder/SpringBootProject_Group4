package com.creatorworks.nexus.notification.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification { // 알림 데이터
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 인덱스(자동 증가)

    private Long senderUserId;   // 보낸 사람 아이디
    private Long targetUserId;   // 받는 사람 아이디
    @Column(nullable = false)
    private String message;      // 메시지 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @Column(nullable = false)
    private String type;         // 알림 타입 (follow, like, comment 등)
    private Boolean isRead;      // 읽음 유무
    @Column(nullable = true)
    private String link;         // 연결 링크(눌렀을 때 이동할 URL)

    private Long productId; // 좋아요 알림의 경우 작품 ID

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isRead == null) {
            this.isRead = false;
        }
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", senderUserId=" + senderUserId +
                ", targetUserId=" + targetUserId +
                ", message='" + message + '\'' +
                ", category=" + category +
                ", type='" + type + '\'' +
                ", isRead=" + isRead +
                ", link='" + link + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 