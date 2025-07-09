package com.creatorworks.nexus.order.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creatorworks.nexus.member.service.IamportService;
import com.creatorworks.nexus.order.service.PaymentService;
import com.creatorworks.nexus.order.service.PointService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment/webhook")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final IamportService iamportService;
    private final PaymentService paymentService;
    private final PointService pointService;

    /**
     * 아임포트 웹훅 처리
     * KG이니시스에서 결제 완료 후 이 엔드포인트로 POST 요청을 보냄
     */
    @PostMapping("/iamport")
    public ResponseEntity<Map<String, Object>> handleIamportWebhook(@RequestBody Map<String, Object> webhookData) {
        try {
            log.info("아임포트 웹훅 수신: {}", webhookData);
            
            String impUid = (String) webhookData.get("imp_uid");
            String merchantUid = (String) webhookData.get("merchant_uid");
            String status = (String) webhookData.get("status");
            
            if (impUid == null || merchantUid == null || status == null) {
                log.error("웹훅 데이터 누락: impUid={}, merchantUid={}, status={}", impUid, merchantUid, status);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "필수 데이터 누락"));
            }
            
            // 결제 상태 확인
            if ("paid".equals(status)) {
                // 결제 성공 처리
                return handlePaymentSuccess(impUid, merchantUid, webhookData);
            } else if ("cancelled".equals(status)) {
                // 결제 취소 처리
                return handlePaymentCancelled(impUid, merchantUid, webhookData);
            } else if ("failed".equals(status)) {
                // 결제 실패 처리
                return handlePaymentFailed(impUid, merchantUid, webhookData);
            } else {
                log.warn("알 수 없는 결제 상태: status={}, impUid={}", status, impUid);
                return ResponseEntity.ok().body(Map.of("success", true, "message", "처리 완료"));
            }
            
        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "서버 오류"));
        }
    }
    
    /**
     * 결제 성공 처리
     */
    private ResponseEntity<Map<String, Object>> handlePaymentSuccess(String impUid, String merchantUid, Map<String, Object> webhookData) {
        try {
            // 중복 처리 방지
            if (paymentService.isPaymentProcessed(impUid)) {
                log.info("이미 처리된 결제: impUid={}", impUid);
                return ResponseEntity.ok().body(Map.of("success", true, "message", "이미 처리됨"));
            }
            
            // 결제 정보 조회
            Map<String, Object> paymentInfo = iamportService.getPaymentInfo(impUid);
            if (paymentInfo == null) {
                log.error("결제 정보 조회 실패: impUid={}", impUid);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "결제 정보 조회 실패"));
            }
            
            Long amount = ((Number) paymentInfo.get("amount")).longValue();
            
            // 결제 검증
            boolean isValid = iamportService.verifyPayment(impUid, merchantUid, amount);
            if (!isValid) {
                log.error("결제 검증 실패: impUid={}", impUid);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "결제 검증 실패"));
            }
            
            // 결제 완료 처리
            paymentService.completePayment(impUid);
            
            // 포인트 충전인 경우 포인트 추가
            if (merchantUid.startsWith("point_")) {
                pointService.completePointCharge(impUid, amount);
                log.info("포인트 충전 완료: impUid={}, amount={}", impUid, amount);
            }
            
            log.info("결제 성공 처리 완료: impUid={}, merchantUid={}, amount={}", impUid, merchantUid, amount);
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "결제 성공 처리 완료",
                "imp_uid", impUid,
                "merchant_uid", merchantUid
            ));
            
        } catch (Exception e) {
            log.error("결제 성공 처리 중 오류: impUid={}, error={}", impUid, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "결제 처리 중 오류"));
        }
    }
    
    /**
     * 결제 취소 처리
     */
    private ResponseEntity<Map<String, Object>> handlePaymentCancelled(String impUid, String merchantUid, Map<String, Object> webhookData) {
        try {
            String reason = (String) webhookData.getOrDefault("reason", "사용자 취소");
            paymentService.failPayment(impUid, reason);
            
            log.info("결제 취소 처리 완료: impUid={}, reason={}", impUid, reason);
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "결제 취소 처리 완료",
                "imp_uid", impUid
            ));
            
        } catch (Exception e) {
            log.error("결제 취소 처리 중 오류: impUid={}, error={}", impUid, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "취소 처리 중 오류"));
        }
    }
    
    /**
     * 결제 실패 처리
     */
    private ResponseEntity<Map<String, Object>> handlePaymentFailed(String impUid, String merchantUid, Map<String, Object> webhookData) {
        try {
            String errorMsg = (String) webhookData.getOrDefault("fail_reason", "결제 실패");
            paymentService.failPayment(impUid, errorMsg);
            
            log.info("결제 실패 처리 완료: impUid={}, error={}", impUid, errorMsg);
            
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "결제 실패 처리 완료",
                "imp_uid", impUid
            ));
            
        } catch (Exception e) {
            log.error("결제 실패 처리 중 오류: impUid={}, error={}", impUid, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "실패 처리 중 오류"));
        }
    }
} 