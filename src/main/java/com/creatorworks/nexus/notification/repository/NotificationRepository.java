package com.creatorworks.nexus.notification.repository;

import com.creatorworks.nexus.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 필요하다면 커스텀 쿼리 메서드도 추가 가능
}
