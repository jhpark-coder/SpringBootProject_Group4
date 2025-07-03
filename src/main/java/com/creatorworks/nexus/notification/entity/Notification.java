package com.creatorworks.nexus.notification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 인덱스(오토인크리먼트)

    private Long senderUserId;   // 보낸 사람 아이디
    private Long targetUserId;   // 받는 사람 아이디
    private String message;      // 메시지 내용
    private Boolean isRead;      // 읽음 유무
    private String link;         // 연결 링크(눌렀을 때 이동할 URL)
    private LocalDateTime createdAt; // 생성일시

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isRead == null) this.isRead = false;
    }
} 