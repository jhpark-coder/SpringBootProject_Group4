package com.creatorworks.nexus.point.controller;

import com.creatorworks.nexus.member.dto.CustomUserDetails;
import com.creatorworks.nexus.point.service.PointChargeService;
import com.creatorworks.nexus.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {
    
    private final PointService pointService;
    private final PointChargeService pointChargeService;
    
    /**
     * 포인트 잔액 조회
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        
        Integer balance = pointService.getBalance(userDetails.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("balance", balance);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 포인트 충전
     */
    @PostMapping("/charge")
    public ResponseEntity<?> chargePoints(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        
        try {
            Integer amount = (Integer) request.get("amount");
            String paymentMethod = (String) request.get("paymentMethod");
            
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "유효하지 않은 충전 금액입니다."));
            }
            
            pointChargeService.chargePoints(userDetails.getUsername(), amount, paymentMethod);
            
            // 충전 후 잔액 조회
            Integer newBalance = pointService.getBalance(userDetails.getUsername());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "포인트 충전이 완료되었습니다.");
            result.put("chargedAmount", amount);
            result.put("newBalance", newBalance);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "포인트 충전 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
}