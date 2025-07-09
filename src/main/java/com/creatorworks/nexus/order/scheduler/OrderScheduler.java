package com.creatorworks.nexus.order.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.creatorworks.nexus.order.entity.Order;
import com.creatorworks.nexus.order.entity.Payment;
import com.creatorworks.nexus.order.service.OrderService;
import com.creatorworks.nexus.order.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * 매일 새벽 2시에 정기결제를 처리합니다.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void processRecurringPayments() {
        log.info("정기결제 처리 시작");
        
        try {
            // 다음 결제일이 임박한 구독 주문들을 조회
            List<Order> upcomingOrders = orderService.findUpcomingSubscriptionOrders();
            
            log.info("정기결제 대상 구독 수: {}", upcomingOrders.size());
            
            for (Order order : upcomingOrders) {
                try {
                    processSubscriptionPayment(order);
                } catch (Exception e) {
                    log.error("개별 구독 정기결제 처리 중 오류: 구독ID={}, 오류={}", 
                            order.getId(), e.getMessage());
                    
                    // 결제 실패 처리
                    Payment payment = order.getPayment();
                    if (payment != null) {
                        paymentService.failPayment(payment.getImpUid(), "정기결제 실패: " + e.getMessage());
                    }
                }
            }
            
            log.info("정기결제 처리 완료");
        } catch (Exception e) {
            log.error("정기결제 처리 중 전체 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 매일 새벽 3시에 결제 실패한 정기결제를 재시도합니다.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void retryFailedPayments() {
        log.info("실패한 정기결제 재시도 시작");
        
        try {
            List<Payment> failedPayments = paymentService.findFailedRecurringPayments();
            
            log.info("재시도 대상 결제 수: {}", failedPayments.size());
            
            for (Payment payment : failedPayments) {
                try {
                    // 재시도 로직 (실제로는 아임포트 API 호출)
                    retryPayment(payment);
                } catch (Exception e) {
                    log.error("개별 결제 재시도 중 오류: paymentId={}, 오류={}", 
                            payment.getId(), e.getMessage());
                }
            }
            
            log.info("실패한 정기결제 재시도 완료");
        } catch (Exception e) {
            log.error("실패한 정기결제 재시도 중 전체 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 매주 일요일 새벽 4시에 주문 통계를 생성합니다.
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    public void generateOrderReport() {
        log.info("주문 통계 리포트 생성 시작");
        
        try {
            // 주간 주문 통계 생성
            LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
            LocalDateTime now = LocalDateTime.now();
            
            // 여기에 통계 생성 로직 추가
            // 예: 주간 매출, 인기 상품, 구독 현황 등
            
            log.info("주문 통계 리포트 생성 완료");
        } catch (Exception e) {
            log.error("주문 통계 리포트 생성 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 개별 구독 정기결제를 처리합니다.
     */
    private void processSubscriptionPayment(Order order) {
        Payment payment = order.getPayment();
        if (payment == null || payment.getCustomerUid() == null) {
            log.warn("고객 UID가 없는 구독: 구독ID={}", order.getId());
            return;
        }

        // 새로운 merchantUid 생성
        String merchantUid = "recurring_" + order.getId() + "_" + System.currentTimeMillis();
        
        // 실제로는 아임포트 API를 호출하여 정기결제를 처리
        // 여기서는 시뮬레이션
        boolean paymentSuccess = simulatePayment(payment.getCustomerUid(), payment.getAmount(), merchantUid);
        
        if (paymentSuccess) {
            // 결제 성공 시 구독 연장
            extendSubscription(order);
            
            // 다음 결제일 설정 (1개월 후)
            LocalDateTime nextBillingDate = LocalDateTime.now().plusMonths(1);
            paymentService.setNextBillingDate(payment.getCustomerUid(), nextBillingDate);
            
            log.info("정기결제 성공: 구독ID={}, 금액={}, 다음결제일={}", 
                    order.getId(), payment.getAmount(), nextBillingDate);
        } else {
            // 결제 실패 처리
            paymentService.failPayment(payment.getImpUid(), "정기결제 실패");
            log.warn("정기결제 실패: 구독ID={}, 금액={}", order.getId(), payment.getAmount());
        }
    }

    /**
     * 결제 재시도를 처리합니다.
     */
    private void retryPayment(Payment payment) {
        // 실제로는 아임포트 API를 호출하여 재시도
        // 여기서는 시뮬레이션
        boolean retrySuccess = simulatePayment(payment.getCustomerUid(), payment.getAmount(), 
                                             payment.getMerchantUid() + "_retry");
        
        if (retrySuccess) {
            paymentService.completePayment(payment.getImpUid());
            log.info("결제 재시도 성공: paymentId={}", payment.getId());
        } else {
            log.warn("결제 재시도 실패: paymentId={}", payment.getId());
        }
    }

    /**
     * 구독을 연장합니다.
     */
    private void extendSubscription(Order order) {
        // 구독 기간을 1개월 연장하는 로직
        // 실제로는 Subscription 엔티티의 extend 메서드를 호출
        log.info("구독 연장: 구독ID={}", order.getId());
    }

    /**
     * 결제 시뮬레이션 (실제로는 아임포트 API 호출)
     */
    private boolean simulatePayment(String customerUid, Long amount, String merchantUid) {
        // 실제 구현에서는 아임포트 API를 호출
        // 여기서는 90% 성공률로 시뮬레이션
        return Math.random() > 0.1;
    }
} 