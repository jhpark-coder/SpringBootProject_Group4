package com.creatorworks.nexus.payment.controller;

import com.creatorworks.nexus.member.dto.CustomUserDetails;
import com.creatorworks.nexus.payment.dto.PaymentRequestDto;
import com.creatorworks.nexus.payment.dto.PaymentResponseDto;
import com.creatorworks.nexus.payment.service.PaymentService;
import com.creatorworks.nexus.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PointService pointService;
    
    /**
     * 포인트 결제 처리
     */
    @PostMapping("/point")
    public ResponseEntity<?> processPointPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PaymentRequestDto requestDto) {
        
        if (userDetails == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }
        
        try {
            // 사용자 이메일로 Member ID를 찾는 로직은 PaymentService에서 처리
            PaymentResponseDto response = paymentService.processPointPayment(userDetails.getUsername(), requestDto);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            result.put("message", "포인트 결제가 완료되었습니다.");
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 포인트 잔액 조회
     */
    @GetMapping("/point/balance")
    public ResponseEntity<?> getPointBalance(@AuthenticationPrincipal CustomUserDetails userDetails) {
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
     * 결제 이력 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long paymentId) {
        try {
            PaymentResponseDto response = paymentService.getPaymentHistory(paymentId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
} 