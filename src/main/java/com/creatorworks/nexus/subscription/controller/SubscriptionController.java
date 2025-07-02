package com.creatorworks.nexus.subscription.controller;

import com.creatorworks.nexus.member.dto.CustomUserDetails;
import com.creatorworks.nexus.subscription.dto.SubscriptionCancelRequestDto;
import com.creatorworks.nexus.subscription.dto.SubscriptionRequestDto;
import com.creatorworks.nexus.subscription.dto.SubscriptionResponseDto;
import com.creatorworks.nexus.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    
    private final SubscriptionService subscriptionService;
    
    /**
     * 구독 신청
     */
    @PostMapping
    public ResponseEntity<?> createSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SubscriptionRequestDto requestDto) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        
        try {
            SubscriptionResponseDto response = subscriptionService.createSubscription(
                    userDetails.getUsername(), requestDto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            result.put("message", "구독이 성공적으로 시작되었습니다.");
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 구독 취소
     */
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<?> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long subscriptionId,
            @RequestBody SubscriptionCancelRequestDto requestDto) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        
        try {
            SubscriptionResponseDto response = subscriptionService.cancelSubscription(
                    userDetails.getUsername(), subscriptionId, requestDto.getReason());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            result.put("message", "구독이 취소되었습니다.");
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 내 구독 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMySubscriptions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        
        try {
            Page<SubscriptionResponseDto> subscriptions = subscriptionService
                    .getSubscriptionsBySubscriber(userDetails.getUsername(), pageable);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", subscriptions);
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 작가별 구독자 목록 조회 (작가용)
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<?> getSubscribersByAuthor(
            @PathVariable Long authorId,
            Pageable pageable) {
        
        try {
            Page<SubscriptionResponseDto> subscriptions = subscriptionService
                    .getSubscriptionsByAuthor(authorId, pageable);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", subscriptions);
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
} 