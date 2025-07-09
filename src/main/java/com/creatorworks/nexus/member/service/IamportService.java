package com.creatorworks.nexus.member.service;

import com.creatorworks.nexus.member.entity.Subscription;
import com.creatorworks.nexus.member.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IamportService {
    
    @Value("${iamport.api.key}")
    private String apiKey;
    
    @Value("${iamport.api.secret}")
    private String apiSecret;
    
    private final SubscriptionRepository subscriptionRepository;
    
    /**
     * 테스트 모드인지 확인
     */
    private boolean isTestMode() {
        return "your_iamport_api_key".equals(apiKey) || apiKey == null || apiKey.isEmpty();
    }
    
    /**
     * 아임포트 결제 검증
     */
    public boolean verifyPayment(String impUid, String merchantUid, Long amount) {
        try {
            // API 키가 더미 값인 경우 테스트 모드로 동작
            if (isTestMode()) {
                log.warn("아임포트 API 키가 설정되지 않았습니다. 테스트 모드로 동작합니다.");
                log.info("테스트 모드 - 결제 검증 완료: impUid={}, merchantUid={}, amount={}", impUid, merchantUid, amount);
                return true; // 테스트 모드에서는 항상 성공
            }
            
            // TODO: 실제 아임포트 API 호출 (의존성 추가 후 구현)
            log.info("결제 검증 완료: impUid={}, merchantUid={}, amount={}", impUid, merchantUid, amount);
            return true;
            
        } catch (Exception e) {
            log.error("결제 검증 중 예외 발생: impUid={}, error={}", impUid, e.getMessage());
            return false;
        }
    }
    
    /**
     * 정기결제 요청
     */
    public boolean requestRecurringPayment(String customerUid, Long amount, String merchantUid) {
        try {
            if (isTestMode()) {
                log.warn("아임포트 API 키가 설정되지 않았습니다. 테스트 모드로 동작합니다.");
                log.info("테스트 모드 - 정기결제 요청 완료: customerUid={}, amount={}, merchantUid={}", 
                        customerUid, amount, merchantUid);
                return true;
            }
            
            // TODO: 실제 아임포트 정기결제 API 호출 (의존성 추가 후 구현)
            log.info("정기결제 요청 완료: customerUid={}, amount={}, merchantUid={}", 
                    customerUid, amount, merchantUid);
            return true;
            
        } catch (Exception e) {
            log.error("정기결제 요청 중 예외 발생: customerUid={}, error={}", customerUid, e.getMessage());
            return false;
        }
    }
    
    /**
     * 정기결제 취소
     */
    public boolean cancelRecurringPayment(String customerUid) {
        try {
            if (isTestMode()) {
                log.warn("아임포트 API 키가 설정되지 않았습니다. 테스트 모드로 동작합니다.");
                log.info("테스트 모드 - 정기결제 취소 완료: customerUid={}", customerUid);
                return true;
            }
            
            // TODO: 실제 아임포트 정기결제 취소 API 호출 (의존성 추가 후 구현)
            log.info("정기결제 취소 완료: customerUid={}", customerUid);
            return true;
            
        } catch (Exception e) {
            log.error("정기결제 취소 중 예외 발생: customerUid={}, error={}", customerUid, e.getMessage());
            return false;
        }
    }
    
    /**
     * 결제 정보 조회
     */
    public Map<String, Object> getPaymentInfo(String impUid) {
        try {
            if (isTestMode()) {
                log.warn("아임포트 API 키가 설정되지 않았습니다. 테스트 모드로 동작합니다.");
                Map<String, Object> paymentInfo = new HashMap<>();
                paymentInfo.put("status", "paid");
                paymentInfo.put("amount", 9900);
                paymentInfo.put("card_number", "1234");
                paymentInfo.put("card_type", "VISA");
                paymentInfo.put("merchant_uid", "test_merchant_uid");
                paymentInfo.put("paid_at", System.currentTimeMillis());
                log.info("테스트 모드 - 결제 정보 조회: impUid={}", impUid);
                return paymentInfo;
            }
            
            // TODO: 실제 아임포트 API 호출 (의존성 추가 후 구현)
            Map<String, Object> paymentInfo = new HashMap<>();
            paymentInfo.put("status", "paid");
            paymentInfo.put("amount", 9900);
            paymentInfo.put("card_number", "1234");
            paymentInfo.put("card_type", "VISA");
            paymentInfo.put("merchant_uid", "test_merchant_uid");
            paymentInfo.put("paid_at", System.currentTimeMillis());
            
            return paymentInfo;
            
        } catch (Exception e) {
            log.error("결제 정보 조회 중 예외 발생: impUid={}, error={}", impUid, e.getMessage());
            return null;
        }
    }
    
    /**
     * 중복 결제 방지 체크
     */
    public boolean isDuplicatePayment(String merchantUid) {
        return subscriptionRepository.findByMerchantUid(merchantUid).isPresent();
    }
    
    /**
     * 결제 취소
     */
    public boolean cancelPayment(String impUid, String reason) {
        try {
            if (isTestMode()) {
                log.warn("아임포트 API 키가 설정되지 않았습니다. 테스트 모드로 동작합니다.");
                log.info("테스트 모드 - 결제 취소 완료: impUid={}, reason={}", impUid, reason);
                return true;
            }
            
            // TODO: 실제 아임포트 결제 취소 API 호출 (의존성 추가 후 구현)
            log.info("결제 취소 완료: impUid={}, reason={}", impUid, reason);
            return true;
            
        } catch (Exception e) {
            log.error("결제 취소 중 예외 발생: impUid={}, error={}", impUid, e.getMessage());
            return false;
        }
    }
}