package com.creatorworks.nexus.member.scheduler;

import com.creatorworks.nexus.member.constant.SubscriptionStatus;
import com.creatorworks.nexus.member.entity.Subscription;
import com.creatorworks.nexus.member.repository.SubscriptionRepository;
import com.creatorworks.nexus.member.service.IamportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionBillingScheduler {
    
    private final SubscriptionRepository subscriptionRepository;
    private final IamportService iamportService;
    
    /**
     * 매일 자정에 실행
     * 정기결제가 필요한 구독 처리
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    @Transactional
    public void processRecurringPayments() {
        log.info("정기결제 처리 스케줄러 시작");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);
            
            // 다음 결제일이 오늘인 활성 구독 조회
            List<Subscription> subscriptionsToBill = subscriptionRepository
                    .findNextBillingSoon(SubscriptionStatus.ACTIVE, now, tomorrow);
            
            log.info("정기결제 대상 구독 수: {}", subscriptionsToBill.size());
            
            for (Subscription subscription : subscriptionsToBill) {
                try {
                    processSubscriptionPayment(subscription);
                } catch (Exception e) {
                    log.error("구독 정기결제 처리 중 오류: 구독ID={}, 오류={}", 
                            subscription.getId(), e.getMessage());
                    
                    // 결제 실패 시 구독 상태를 PENDING으로 변경
                    subscription.setStatus(SubscriptionStatus.PENDING);
                    subscriptionRepository.save(subscription);
                    
                    // 결제 실패 알림 발송
                    sendPaymentFailureNotification(subscription);
                }
            }
            
            log.info("정기결제 처리 완료");
            
        } catch (Exception e) {
            log.error("정기결제 처리 중 오류 발생", e);
        }
    }
    
    /**
     * 개별 구독 정기결제 처리
     */
    private void processSubscriptionPayment(Subscription subscription) {
        if (subscription.getCustomerUid() == null) {
            log.warn("고객 UID가 없는 구독: 구독ID={}", subscription.getId());
            return;
        }
        
        // 정기결제 요청
        String merchantUid = "recurring_" + subscription.getId() + "_" + System.currentTimeMillis();
        boolean paymentSuccess = iamportService.requestRecurringPayment(
                subscription.getCustomerUid(), 
                subscription.getAmount(), 
                merchantUid
        );
        
        if (paymentSuccess) {
            // 결제 성공 시 구독 연장
            subscription.extend(1); // 1개월 연장
            subscriptionRepository.save(subscription);
            
            log.info("정기결제 성공: 구독ID={}, 금액={}, 다음결제일={}", 
                    subscription.getId(), subscription.getAmount(), subscription.getNextBillingDate());
            
            // 결제 성공 알림 발송
            sendPaymentSuccessNotification(subscription);
            
        } else {
            throw new RuntimeException("정기결제 실패");
        }
    }
    
    /**
     * 결제 성공 알림 발송
     */
    private void sendPaymentSuccessNotification(Subscription subscription) {
        // TODO: 실제 알림 발송 로직 구현
        log.info("정기결제 성공 알림 발송: 구독자ID={}, 작가ID={}, 금액={}", 
                subscription.getSubscriber().getId(), 
                subscription.getAuthor().getId(), 
                subscription.getAmount());
    }
    
    /**
     * 결제 실패 알림 발송
     */
    private void sendPaymentFailureNotification(Subscription subscription) {
        // TODO: 실제 알림 발송 로직 구현
        log.info("정기결제 실패 알림 발송: 구독자ID={}, 작가ID={}, 금액={}", 
                subscription.getSubscriber().getId(), 
                subscription.getAuthor().getId(), 
                subscription.getAmount());
    }
    
    /**
     * 매일 오후 2시에 실행
     * 결제 실패한 구독자에게 재시도 알림 발송
     */
    @Scheduled(cron = "0 0 14 * * ?") // 매일 오후 2시
    public void sendRetryPaymentNotifications() {
        log.info("결제 재시도 알림 발송 스케줄러 시작");
        
        try {
            // PENDING 상태인 구독 조회
            List<Subscription> pendingSubscriptions = subscriptionRepository
                    .findByStatus(SubscriptionStatus.PENDING);
            
            log.info("결제 재시도 대상 구독 수: {}", pendingSubscriptions.size());
            
            for (Subscription subscription : pendingSubscriptions) {
                sendRetryPaymentNotification(subscription);
            }
            
            log.info("결제 재시도 알림 발송 완료");
            
        } catch (Exception e) {
            log.error("결제 재시도 알림 발송 중 오류 발생", e);
        }
    }
    
    /**
     * 결제 재시도 알림 발송
     */
    private void sendRetryPaymentNotification(Subscription subscription) {
        // TODO: 실제 알림 발송 로직 구현
        log.info("결제 재시도 알림 발송: 구독자ID={}, 작가ID={}", 
                subscription.getSubscriber().getId(), 
                subscription.getAuthor().getId());
    }
} 