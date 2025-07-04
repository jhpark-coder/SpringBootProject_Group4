package com.creatorworks.nexus.notification.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.notification.entity.Notification;
import com.creatorworks.nexus.notification.service.NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        long unreadCount = notificationService.getUnreadNotificationCount(member.getId());
        
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

        List<Notification> notifications = notificationService.getNotificationsByUserId(member.getId());
        
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
        notificationService.markAllAsRead(member.getId());
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