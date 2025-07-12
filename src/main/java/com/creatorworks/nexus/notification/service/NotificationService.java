package com.creatorworks.nexus.notification.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.creatorworks.nexus.notification.dto.FollowNotificationRequest;
import com.creatorworks.nexus.notification.dto.InquiryNotificationRequest;
import com.creatorworks.nexus.notification.dto.PaymentNotificationRequest;
import com.creatorworks.nexus.notification.dto.ReviewNotificationRequest;
import com.creatorworks.nexus.notification.dto.SellerRequestNotificationRequest;
import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.repository.NotificationRepository;

@Service
public class NotificationService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NESTJS_NOTIFY_URL = "http://localhost:3000/api/notifications/create";
    private static final String NESTJS_ADMIN_NOTIFY_URL = "http://localhost:3000/api/notifications/admin/create";
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(FollowNotificationRequest dto) {
        try {
            restTemplate.postForObject(NESTJS_NOTIFY_URL, dto, Void.class);
        } catch (Exception e) {
            // 실시간 알림 서버 에러는 로깅만 하고 계속 진행
            System.err.println("실시간 알림 전송 실패: " + e.getMessage());
        }
    }

    public void sendNotification(ReviewNotificationRequest dto) {
        try {
            restTemplate.postForObject(NESTJS_NOTIFY_URL, dto, Void.class);
        } catch (Exception e) {
            // 실시간 알림 서버 에러는 로깅만 하고 계속 진행
            System.err.println("실시간 알림 전송 실패: " + e.getMessage());
        }
    }

    public void sendNotification(InquiryNotificationRequest dto) {
        try {
            restTemplate.postForObject(NESTJS_NOTIFY_URL, dto, Void.class);
        } catch (Exception e) {
            // 실시간 알림 서버 에러는 로깅만 하고 계속 진행
            System.err.println("실시간 알림 전송 실패: " + e.getMessage());
        }
    }

    public void sendNotificationToAdminGroup(SellerRequestNotificationRequest dto) {
        try {
            restTemplate.postForObject(NESTJS_ADMIN_NOTIFY_URL, dto, Void.class);
        } catch (Exception e) {
            // 실시간 알림 서버 에러는 로깅만 하고 계속 진행
            System.err.println("관리자 실시간 알림 전송 실패: " + e.getMessage());
        }
    }

    // 결제 알림 전송 (실시간)
    public void sendPaymentNotification(PaymentNotificationRequest dto) {
        try {
            restTemplate.postForObject(NESTJS_NOTIFY_URL, dto, Void.class);
        } catch (Exception e) {
            // 실시간 알림 서버 에러는 로깅만 하고 계속 진행
            System.err.println("결제 실시간 알림 전송 실패: " + e.getMessage());
        }
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

    // 관리자의 읽지 않은 알림 개수 조회 (개인 + 관리자 알림)
    public long getUnreadNotificationCountForAdmin(Long userId) {
        return notificationRepository.countUserAndAdminUnreadNotifications(userId, com.creatorworks.nexus.notification.entity.NotificationCategory.ADMIN);
    }

    // 작가 신청 알림을 DB에 저장하는 메서드
    public Notification saveSellerRequestNotification(SellerRequestNotificationRequest dto, String link) {
        System.out.println("=== [알림 DB 저장] saveSellerRequestNotification 호출 ===");
        System.out.println("DTO 정보: targetUserId=" + dto.getTargetUserId() + ", message=" + dto.getMessage());
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 1; i < Math.min(stackTrace.length, 8); i++) {
            System.out.println("  at " + stackTrace[i]);
        }
        Notification notification = Notification.builder()
                .senderUserId(0L) // 시스템에서 보내는 알림이므로 0으로 설정
                .targetUserId(dto.getTargetUserId())
                .message(dto.getMessage())
                .type(dto.getType())
                .category(dto.getCategory())
                .isRead(false)
                .link(link)
                .build();
        return notificationRepository.save(notification);
    }

    // 결제 알림을 DB에 저장하는 메서드
    public Notification savePaymentNotification(PaymentNotificationRequest dto, String link) {
        System.out.println("=== [알림 DB 저장] savePaymentNotification 호출 ===");
        System.out.println("DTO 정보: targetUserId=" + dto.getTargetUserId() + ", message=" + dto.getMessage());
        Notification notification = Notification.builder()
                .senderUserId(0L) // 시스템에서 보내는 알림이므로 0으로 설정
                .targetUserId(dto.getTargetUserId())
                .message(dto.getMessage())
                .type(dto.getType())
                .category(dto.getCategory())
                .isRead(false)
                .link(link)
                .build();
        return notificationRepository.save(notification);
    }

    // 좋아요 알림을 DB에 저장하는 메서드 (최초 좋아요에만 알림)
    public Notification saveLikeNotification(Long senderUserId, Long targetUserId, Long productId, String message, String link) {
        // 중복 체크: sender, target, type, productId
        boolean exists = notificationRepository.existsBySenderUserIdAndTargetUserIdAndTypeAndProductId(
            senderUserId, targetUserId, "like", productId);
        if (exists) {
            // 이미 알림이 존재하면 null 반환 (알림 생성하지 않음)
            return null;
        }
        Notification notification = Notification.builder()
                .senderUserId(senderUserId)
                .targetUserId(targetUserId)
                .message(message)
                .type("like")
                .category(com.creatorworks.nexus.notification.entity.NotificationCategory.SOCIAL)
                .isRead(false)
                .link(link)
                .productId(productId)
                .build();
        return notificationRepository.save(notification);
    }

    // 후기 알림을 DB에 저장하는 메서드
    public Notification saveReviewNotification(Long senderUserId, Long targetUserId, Long productId, String message, String link, int rating) {
        // 중복 체크: sender, target, type, productId
        boolean exists = notificationRepository.existsBySenderUserIdAndTargetUserIdAndTypeAndProductId(
            senderUserId, targetUserId, "review", productId);
        if (exists) {
            // 이미 알림이 존재하면 null 반환 (알림 생성하지 않음)
            return null;
        }
        Notification notification = Notification.builder()
                .senderUserId(senderUserId)
                .targetUserId(targetUserId)
                .message(message)
                .type("review")
                .category(com.creatorworks.nexus.notification.entity.NotificationCategory.SOCIAL)
                .isRead(false)
                .link(link)
                .productId(productId)
                .build();
        return notificationRepository.save(notification);
    }

    // 문의 알림을 DB에 저장하는 메서드
    public Notification saveInquiryNotification(Long senderUserId, Long targetUserId, Long productId, String message, String link) {
        // 문의는 같은 상품에 여러 번 달 수 있으므로 중복 체크하지 않음
        Notification notification = Notification.builder()
                .senderUserId(senderUserId)
                .targetUserId(targetUserId)
                .message(message)
                .type("inquiry")
                .category(com.creatorworks.nexus.notification.entity.NotificationCategory.SOCIAL)
                .isRead(false)
                .link(link)
                .productId(productId)
                .build();
        return notificationRepository.save(notification);
    }

    // 사용자의 알림 목록 조회
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByTargetUserIdOrderByCreatedAtDesc(userId);
    }

    // 관리자의 알림 목록 조회 (개인 + 관리자 알림)
    public List<Notification> getNotificationsForAdmin(Long userId) {
        return notificationRepository.findUserAndAdminNotifications(userId, com.creatorworks.nexus.notification.entity.NotificationCategory.ADMIN);
    }

    // 전체 알림 읽음 처리
    public void markAllAsRead(Long userId) {
        List<Notification> notis = notificationRepository.findByTargetUserIdAndIsReadFalse(userId);
        for (Notification n : notis) n.setIsRead(true);
        notificationRepository.saveAll(notis);
    }

    // 관리자의 전체 알림 읽음 처리 (개인 + 관리자 알림)
    public void markAllAsReadForAdmin(Long userId) {
        // 개인 알림 읽음 처리
        List<Notification> personalNotis = notificationRepository.findByTargetUserIdAndIsReadFalse(userId);
        for (Notification n : personalNotis) n.setIsRead(true);
        
        // 관리자 알림 읽음 처리 (TARGET_USER_ID = 0, ADMIN 카테고리)
        List<Notification> adminNotis = notificationRepository.findByTargetUserIdAndCategoryOrderByCreatedAtDesc(0L, com.creatorworks.nexus.notification.entity.NotificationCategory.ADMIN);
        List<Notification> unreadAdminNotis = adminNotis.stream()
                .filter(n -> !n.getIsRead())
                .collect(java.util.stream.Collectors.toList());
        for (Notification n : unreadAdminNotis) n.setIsRead(true);
        
        // 모든 변경사항 저장
        List<Notification> allNotis = new ArrayList<>();
        allNotis.addAll(personalNotis);
        allNotis.addAll(unreadAdminNotis);
        if (!allNotis.isEmpty()) {
            notificationRepository.saveAll(allNotis);
        }
    }

    // 개별 알림 읽음 처리
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return;
        }
        
        // 개인 알림이거나 관리자 알림인 경우 읽음 처리
        boolean isPersonalNotification = notification.getTargetUserId().equals(userId);
        boolean isAdminNotification = notification.getTargetUserId().equals(0L) && 
                                    notification.getCategory() == com.creatorworks.nexus.notification.entity.NotificationCategory.ADMIN;
        
        if (isPersonalNotification || isAdminNotification) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
} 