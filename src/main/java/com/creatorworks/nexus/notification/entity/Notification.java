package com.creatorworks.nexus.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
} 