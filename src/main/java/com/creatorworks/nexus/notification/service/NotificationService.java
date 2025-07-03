package com.creatorworks.nexus.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.creatorworks.nexus.notification.dto.FollowNotificationRequest;
import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.repository.NotificationRepository;

@Service
public class NotificationService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NESTJS_NOTIFY_URL = "http://localhost:3000/notification/follow";
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(FollowNotificationRequest dto) {
        restTemplate.postForObject(NESTJS_NOTIFY_URL, dto, Void.class);
    }

    // 알림을 DB에 저장하는 메서드
    public Notification saveNotification(FollowNotificationRequest dto, String link) {
        Notification notification = Notification.builder()
                .senderUserId(dto.getSenderUserId())
                .targetUserId(dto.getTargetUserId())
                .message(dto.getMessage())
                .isRead(false)
                .link(link)
                .build();
        return notificationRepository.save(notification);
    }
} 