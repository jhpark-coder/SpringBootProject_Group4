package com.creatorworks.nexus.subscription.scheduler;

import com.creatorworks.nexus.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {
    
    private final SubscriptionService subscriptionService;
    
    /**
     * 매일 자정에 구독 갱신 처리
     * cron = "초 분 시 일 월 요일"
     * "0 0 0 * * *" = 매일 00:00:00
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void processSubscriptionRenewals() {
        log.info("=== 구독 갱신 스케줄러 시작 ===");
        try {
            subscriptionService.processSubscriptionRenewals();
            log.info("=== 구독 갱신 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("구독 갱신 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 매일 자정에 만료된 구독 처리
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void processExpiredSubscriptions() {
        log.info("=== 만료된 구독 처리 스케줄러 시작 ===");
        try {
            subscriptionService.processExpiredSubscriptions();
            log.info("=== 만료된 구독 처리 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("만료된 구독 처리 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 매시간마다 구독 상태 체크 (테스트용)
     * 실제 운영에서는 매일 자정만 사용
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkSubscriptionStatus() {
        log.info("=== 구독 상태 체크 스케줄러 시작 ===");
        try {
            // 구독 갱신 처리
            subscriptionService.processSubscriptionRenewals();
            // 만료된 구독 처리
            subscriptionService.processExpiredSubscriptions();
            log.info("=== 구독 상태 체크 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("구독 상태 체크 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
} 