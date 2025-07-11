package com.creatorworks.nexus.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.entity.NotificationCategory;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 필요하다면 커스텀 쿼리 메서드도 추가 가능
    long countByTargetUserIdAndIsReadFalse(Long targetUserId);
    
    // 같은 사용자 간의 팔로우 알림이 이미 존재하는지 확인
    boolean existsBySenderUserIdAndTargetUserIdAndType(Long senderUserId, Long targetUserId, String type);
    
    // 사용자의 알림 목록 조회 (최신순)
    List<Notification> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
    
    // 특정 사용자의 모든 안읽은 알림 조회
    List<Notification> findByTargetUserIdAndIsReadFalse(Long targetUserId);
    
    // 관리자 알림 조회 (TARGET_USER_ID = 0 또는 ADMIN 카테고리)
    List<Notification> findByTargetUserIdAndCategoryOrderByCreatedAtDesc(Long targetUserId, NotificationCategory category);
    
    // 관리자용: 개인 알림 + 관리자 알림 통합 조회
    @Query("SELECT n FROM Notification n WHERE (n.targetUserId = :userId OR (n.targetUserId = 0 AND n.category = :adminCategory)) ORDER BY n.createdAt DESC")
    List<Notification> findUserAndAdminNotifications(@Param("userId") Long userId, @Param("adminCategory") NotificationCategory adminCategory);
    
    // 관리자용: 개인 + 관리자 알림의 안읽은 개수 조회
    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.targetUserId = :userId OR (n.targetUserId = 0 AND n.category = :adminCategory)) AND n.isRead = false")
    long countUserAndAdminUnreadNotifications(@Param("userId") Long userId, @Param("adminCategory") NotificationCategory adminCategory);

    // 좋아요 알림 중복 체크 (sender, target, type, productId)
    boolean existsBySenderUserIdAndTargetUserIdAndTypeAndProductId(Long senderUserId, Long targetUserId, String type, Long productId);
}
