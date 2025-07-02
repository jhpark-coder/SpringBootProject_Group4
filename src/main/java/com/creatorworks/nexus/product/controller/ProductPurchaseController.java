package com.creatorworks.nexus.product.controller;

import com.creatorworks.nexus.member.dto.CustomUserDetails;
import com.creatorworks.nexus.payment.dto.PaymentRequestDto;
import com.creatorworks.nexus.payment.service.PaymentService;
import com.creatorworks.nexus.product.dto.PointPurchaseRequest;
import com.creatorworks.nexus.product.dto.SubscriptionRequest;
import com.creatorworks.nexus.product.service.ProductPurchaseService;
import com.creatorworks.nexus.subscription.dto.SubscriptionRequestDto;
import com.creatorworks.nexus.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductPurchaseController {

    private final ProductPurchaseService productPurchaseService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    /**
     * 포인트로 상품 구매
     */
    @PostMapping("/{productId}/purchase/point")
    public ResponseEntity<Map<String, Object>> purchaseWithPoints(
            @PathVariable Long productId,
            @RequestBody PointPurchaseRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        if (principal == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            // 새로운 PaymentService 사용
            PaymentRequestDto paymentRequest = new PaymentRequestDto();
            paymentRequest.setProductId(productId);
            paymentRequest.setAmount(request.getPrice());
            paymentRequest.setPaymentMethod("POINT");
            paymentRequest.setDescription("상품 구매: " + productId);
            
            var response = paymentService.processPointPayment(principal.getUsername(), paymentRequest);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "포인트 결제가 완료되었습니다.");
            result.put("remainingBalance", response.getRemainingBalance());
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 정기구독 신청
     */
    @PostMapping("/{productId}/subscription")
    public ResponseEntity<Map<String, Object>> subscribeToAuthor(
            @PathVariable Long productId,
            @RequestBody SubscriptionRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {
        
        if (principal == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));
        }

        try {
            // 새로운 SubscriptionService 사용
            SubscriptionRequestDto subscriptionRequest = new SubscriptionRequestDto();
            subscriptionRequest.setProductId(productId);
            subscriptionRequest.setAuthorId(request.getAuthorId());
            subscriptionRequest.setMonthlyPrice(9900); // 기본 월 구독료
            subscriptionRequest.setPaymentMethod("POINT");
            subscriptionRequest.setAutoRenewal(true);
            
            var response = subscriptionService.createSubscription(principal.getUsername(), subscriptionRequest);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "구독이 성공적으로 시작되었습니다.");
            result.put("subscriptionId", response.getSubscriptionId());
            result.put("authorName", response.getAuthorName());
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
} 