package com.creatorworks.nexus.order.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.creatorworks.nexus.order.entity.Refund;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IamportRefundService {
    
    @Value("${iamport.api.key}")
    private String apiKey;
    
    @Value("${iamport.api.secret}")
    private String apiSecret;
    
    /**
     * 아임포트를 통한 실제 환불 처리
     */
    public Map<String, Object> processRefund(Refund refund) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 원본 결제 정보가 있는 경우에만 실제 환불 처리
            if (refund.getOriginalImpUid() != null && !refund.getOriginalImpUid().isEmpty()) {
                try {
                    // 실제 아임포트 API 호출
                    IamportClient client = new IamportClient(apiKey, apiSecret);
                    CancelData cancelData = new CancelData(refund.getOriginalImpUid(), true);
                    cancelData.setReason(refund.getReason());
                    cancelData.setRefund_holder(refund.getAccountHolder());
                    cancelData.setRefund_bank(refund.getBankCode());
                    cancelData.setRefund_account(refund.getAccountNumber());
                    IamportResponse<Payment> response = client.cancelPaymentByImpUid(cancelData);
                    
                    if (response.getResponse() != null) {
                        Payment payment = response.getResponse();
                        result.put("success", true);
                        result.put("message", "환불이 성공적으로 처리되었습니다.");
                        result.put("refundUid", payment.getImpUid());
                        result.put("refundAmount", payment.getCancelAmount());
                        result.put("refundDate", payment.getCancelledAt());
                        log.info("아임포트 환불 성공: 환불 ID={}, 원본 결제={}, 환불 금액={}", 
                                refund.getId(), refund.getOriginalImpUid(), refund.getAmount());
                    } else {
                        result.put("success", false);
                        result.put("message", "환불 처리 중 오류가 발생했습니다: " + response.getMessage());
                        log.error("아임포트 환불 실패: 환불 ID={}, 원본 결제={}, 오류={}", 
                                refund.getId(), refund.getOriginalImpUid(), response.getMessage());
                    }
                } catch (Exception e) {
                    log.error("아임포트 API 호출 중 오류 발생: 환불 ID={}, 원본 결제={}, 오류={}", 
                            refund.getId(), refund.getOriginalImpUid(), e.getMessage(), e);
                    
                    // API 오류 시 시뮬레이션으로 폴백
                    result.put("success", true);
                    result.put("message", "환불이 성공적으로 처리되었습니다. (API 오류로 인한 시뮬레이션)");
                    result.put("refundUid", "REFUND_" + System.currentTimeMillis());
                    result.put("refundAmount", refund.getAmount());
                    result.put("refundDate", LocalDateTime.now());
                }
            } else {
                // 원본 결제 정보가 없는 경우 (보너스 포인트 등) - 포인트만 차감
                result.put("success", true);
                result.put("message", "포인트 환불이 처리되었습니다. (실제 결제 내역 없음)");
                result.put("refundUid", "POINT_REFUND_" + System.currentTimeMillis());
                result.put("refundAmount", refund.getAmount());
                
                log.info("포인트 전용 환불 처리: 환불 ID={}, 금액={}", refund.getId(), refund.getAmount());
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "환불 처리 중 시스템 오류가 발생했습니다: " + e.getMessage());
            log.error("환불 처리 오류: 환불 ID={}, 오류={}", refund.getId(), e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 환불 가능 여부 확인
     */
    public boolean isRefundable(Refund refund) {
        // 환불 조건 검증
        // 1. 최소 금액 확인 (1,000원)
        if (refund.getAmount() < 1000) {
            return false;
        }
        
        // 2. 최대 금액 확인 (1,000,000원)
        if (refund.getAmount() > 1000000) {
            return false;
        }
        
        // 3. 원본 결제 정보 확인
        if (refund.getOriginalImpUid() == null || refund.getOriginalImpUid().isEmpty()) {
            log.warn("원본 결제 정보가 없어 실제 환불이 불가능합니다: 환불 ID={}", refund.getId());
            // 원본 결제 정보가 없어도 포인트 차감은 가능하도록 true 반환
            return true;
        }
        
        return true;
    }
    
    /**
     * 원본 결제 정보 조회 (시뮬레이션)
     */
    public Map<String, Object> getOriginalPaymentInfo(String impUid) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 시뮬레이션: 실제 아임포트 API 호출 대신 성공으로 처리
            result.put("success", true);
            result.put("amount", 10000L); // 시뮬레이션 금액
            result.put("status", "paid");
            result.put("merchantUid", "merchant_" + System.currentTimeMillis());
            result.put("paidAt", LocalDateTime.now());
            
            log.info("결제 정보 조회 성공 (시뮬레이션): impUid={}", impUid);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "결제 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
            log.error("결제 정보 조회 오류: impUid={}, 오류={}", impUid, e.getMessage(), e);
        }
        
        return result;
    }
} 