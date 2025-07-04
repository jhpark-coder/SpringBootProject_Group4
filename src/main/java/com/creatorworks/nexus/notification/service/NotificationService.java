package com.creatorworks.nexus.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.creatorworks.nexus.notification.dto.FollowNotificationRequest;
import com.creatorworks.nexus.notification.dto.SellerRequestNotificationRequest;
import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.entity.NotificationCategory;
import com.creatorworks.nexus.notification.repository.NotificationRepository;
import java.util.List;

@Service
public class NotificationService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NESTJS_NOTIFY_URL = "http://localhost:3000/notification/follow";
    private static final String NESTJS_ADMIN_NOTIFY_URL = "http://localhost:3000/notification/admin";
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(FollowNotificationRequest dto) {
        restTemplate.postForObject(NESTJS_NOTIFY_URL, dto, Void.class);
    }

    public void sendNotificationToAdminGroup(SellerRequestNotificationRequest dto) {
        restTemplate.postForObject(NESTJS_ADMIN_NOTIFY_URL, dto, Void.class);
    }

    // 알림을 DB에 저장하는 메서드
    public Notification saveNotification(FollowNotificationRequest dto, String link) {
        // 팔로우 알림의 경우 중복 체크
        if ("follow".equals(dto.getType())) {
            // 같은 사용자 간의 팔로우 알림이 이미 존재하는지 확인
            boolean exists = notificationRepository.existsBySenderUserIdAndTargetUserIdAndType(
                dto.getSenderUserId(), dto.getTargetUserId(), "follow");
            
            if (exists) {
                // 이미 팔로우 알림이 존재하면 null 반환 (알림 생성하지 않음)
                return null;
            }
        }
        
        Notification notification = Notification.builder()
                .senderUserId(dto.getSenderUserId())
                .targetUserId(dto.getTargetUserId())
                .message(dto.getMessage())
                .type(dto.getType())
                .category(dto.getCategory())
                .isRead(false)
                .link(link)
                .build();
        return notificationRepository.save(notification);
    }

    // 사용자의 읽지 않은 알림 개수 조회
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByTargetUserIdAndIsReadFalse(userId);
    }

    // 작가 신청 알림을 DB에 저장하는 메서드
    public Notification saveSellerRequestNotification(SellerRequestNotificationRequest dto, String link) {
        Notification notification = Notification.builder()
                .senderUserId(0L) // 시스템에서 보내는 알림이므로 0으로 설정
                .targetUserId(dto.getTargetUserId())
                .message(dto.getMessage())
                .type(dto.getType())
                .category(dto.getCategory())
                .isRead(false)
                .link(link)
                .build();
        
        // DB에 저장하고 바로 반환
        return notificationRepository.save(notification);
    }

    // 사용자의 알림 목록 조회
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByTargetUserIdOrderByCreatedAtDesc(userId);
    }

    // 전체 알림 읽음 처리
    public void markAllAsRead(Long userId) {
        List<Notification> notis = notificationRepository.findByTargetUserIdAndIsReadFalse(userId);
        for (Notification n : notis) n.setIsRead(true);
        notificationRepository.saveAll(notis);
    }

    // 개별 알림 읽음 처리
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getTargetUserId().equals(userId)) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
} 