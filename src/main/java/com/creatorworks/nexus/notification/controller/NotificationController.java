package com.creatorworks.nexus.notification.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping("/count") // 알림 개수 조회
    public ResponseEntity<Map<String, Object>> getUnreadNotificationCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        String userEmail = authentication.getName();
        Member member = memberRepository.findByEmail(userEmail);

        if (member == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        // 관리자 권한 확인
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        
        long unreadCount;
        if (isAdmin) {
            // 관리자의 경우 개인 알림 + 관리자 알림 개수 조회
            unreadCount = notificationService.getUnreadNotificationCountForAdmin(member.getId());
        } else {
            // 일반 사용자의 경우 개인 알림만 조회
            unreadCount = notificationService.getUnreadNotificationCount(member.getId());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", unreadCount);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list") // 알림 목록 조회
    public ResponseEntity<Map<String, Object>> getNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", new ArrayList<>());
            return ResponseEntity.ok(response);
        }

        String userEmail = authentication.getName();
        Member member = memberRepository.findByEmail(userEmail);

        if (member == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", new ArrayList<>());
            return ResponseEntity.ok(response);
        }

        // 관리자 권한 확인
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        
        List<Notification> notifications;
        if (isAdmin) {
            // 관리자의 경우 개인 알림 + 관리자 알림 조회
            notifications = notificationService.getNotificationsForAdmin(member.getId());
        } else {
            // 일반 사용자의 경우 개인 알림만 조회
            notifications = notificationService.getNotificationsByUserId(member.getId());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = authentication.getName();
        Member member = memberRepository.findByEmail(userEmail);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        
        // 관리자 권한 확인
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            // 관리자의 경우 개인 + 관리자 알림 모두 읽음 처리
            notificationService.markAllAsReadForAdmin(member.getId());
        } else {
            // 일반 사용자의 경우 개인 알림만 읽음 처리
            notificationService.markAllAsRead(member.getId());
        }
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(401).build();
        }
        
        String userEmail = authentication.getName();
        Member member = memberRepository.findByEmail(userEmail);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        
        notificationService.markAsRead(id, member.getId());
        return ResponseEntity.ok().build();
    }
} 