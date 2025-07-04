package com.creatorworks.nexus.notification.repository;

import com.creatorworks.nexus.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 필요하다면 커스텀 쿼리 메서드도 추가 가능
    long countByTargetUserIdAndIsReadFalse(Long targetUserId);
    
    // 같은 사용자 간의 팔로우 알림이 이미 존재하는지 확인
    boolean existsBySenderUserIdAndTargetUserIdAndType(Long senderUserId, Long targetUserId, String type);
    
    // 사용자의 알림 목록 조회 (최신순)
    List<Notification> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
    
    // 특정 사용자의 모든 안읽은 알림 조회
    List<Notification> findByTargetUserIdAndIsReadFalse(Long targetUserId);
}
