package com.creatorworks.nexus.member.scheduler;

import com.creatorworks.nexus.member.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {
    
    private final SubscriptionService subscriptionService;
    
    /**
     * 매일 오전 9시에 실행
     * 만료 임박 구독자에게 알림 발송
     */
    @Scheduled(cron = "0 0 9 * * ?") // 매일 오전 9시
    public void sendExpirationNotifications() {
        log.info("구독 만료 임박 알림 발송 스케줄러 시작");
        try {
            subscriptionService.sendExpirationNotifications();
            log.info("구독 만료 임박 알림 발송 완료");
        } catch (Exception e) {
            log.error("구독 만료 임박 알림 발송 중 오류 발생", e);
        }
    }
    
    /**
     * 매일 자정에 실행
     * 만료된 구독 비활성화
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void deactivateExpiredSubscriptions() {
        log.info("만료된 구독 비활성화 스케줄러 시작");
        try {
            subscriptionService.deactivateExpiredSubscriptions();
            log.info("만료된 구독 비활성화 완료");
        } catch (Exception e) {
            log.error("만료된 구독 비활성화 중 오류 발생", e);
        }
    }
    
    /**
     * 매일 오후 3시에 실행
     * 다음 결제일 임박 구독자에게 알림 발송
     */
    @Scheduled(cron = "0 0 15 * * ?") // 매일 오후 3시
    public void sendNextBillingNotifications() {
        log.info("다음 결제일 임박 알림 발송 스케줄러 시작");
        try {
            // TODO: 다음 결제일 임박 알림 발송 로직 구현
            log.info("다음 결제일 임박 알림 발송 완료");
        } catch (Exception e) {
            log.error("다음 결제일 임박 알림 발송 중 오류 발생", e);
        }
    }
    
    /**
     * 매주 월요일 오전 10시에 실행
     * 구독 통계 리포트 생성
     */
    @Scheduled(cron = "0 0 10 ? * MON") // 매주 월요일 오전 10시
    public void generateSubscriptionReport() {
        log.info("구독 통계 리포트 생성 스케줄러 시작");
        try {
            // TODO: 구독 통계 리포트 생성 로직 구현
            log.info("구독 통계 리포트 생성 완료");
        } catch (Exception e) {
            log.error("구독 통계 리포트 생성 중 오류 발생", e);
        }
    }
} 