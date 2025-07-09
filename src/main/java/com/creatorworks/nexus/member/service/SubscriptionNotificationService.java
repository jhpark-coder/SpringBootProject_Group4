package com.creatorworks.nexus.member.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creatorworks.nexus.member.constant.SubscriptionStatus;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.entity.Subscription;
import com.creatorworks.nexus.member.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SubscriptionNotificationService {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * 구독 만료 임박 알림 발송
     */
    @Transactional
    public void sendExpiringSoonNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekLater = now.plusDays(7);

            // 7일 이내 만료되는 활성 구독 조회
            List<Subscription> expiringSubscriptions =
                    subscriptionRepository.findExpiringSoon(SubscriptionStatus.ACTIVE, now, weekLater);

            log.info("만료 임박 구독 수: {}", expiringSubscriptions.size());

            for (Subscription subscription : expiringSubscriptions) {
                try {
                    sendExpiringSoonNotification(subscription);
                } catch (Exception e) {
                    log.error("개별 구독 만료 임박 알림 발송 중 오류: 구독ID={}", subscription.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("구독 만료 임박 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 개별 구독 만료 임박 알림 발송
     */
    private void sendExpiringSoonNotification(Subscription subscription) {
        Member subscriber = subscription.getSubscriber();
        Member author = subscription.getAuthor();

        // TODO: 실제 알림 발송 로직 구현
        // 1. 이메일 알림
        // 2. 푸시 알림
        // 3. SMS 알림

        log.info("구독 만료 임박 알림 발송: 구독자={}, 작가={}, 만료일={}",
                subscriber.getEmail(), author.getEmail(), subscription.getEndDate());

        // 임시로 콘솔에 알림 메시지 출력
        System.out.println("=== 구독 만료 임박 알림 ===");
        System.out.println("구독자: " + subscriber.getEmail());
        System.out.println("작가: " + author.getEmail());
        System.out.println("만료일: " + subscription.getEndDate());
        System.out.println("메시지: 구독이 곧 만료됩니다. 연장하시겠습니까?");
        System.out.println("==========================");
    }

    /**
     * 구독 만료 알림 발송
     */
    @Transactional
    public void sendExpiredNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 만료된 활성 구독 조회
            List<Subscription> expiredSubscriptions =
                    subscriptionRepository.findExpired(SubscriptionStatus.ACTIVE, now);

            log.info("만료된 구독 수: {}", expiredSubscriptions.size());

            for (Subscription subscription : expiredSubscriptions) {
                try {
                    sendExpiredNotification(subscription);
                } catch (Exception e) {
                    log.error("개별 구독 만료 알림 발송 중 오류: 구독ID={}", subscription.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("구독 만료 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 개별 구독 만료 알림 발송
     */
    private void sendExpiredNotification(Subscription subscription) {
        Member subscriber = subscription.getSubscriber();
        Member author = subscription.getAuthor();

        // TODO: 실제 알림 발송 로직 구현

        log.info("구독 만료 알림 발송: 구독자={}, 작가={}, 만료일={}",
                subscriber.getEmail(), author.getEmail(), subscription.getEndDate());

        // 임시로 콘솔에 알림 메시지 출력
        System.out.println("=== 구독 만료 알림 ===");
        System.out.println("구독자: " + subscriber.getEmail());
        System.out.println("작가: " + author.getEmail());
        System.out.println("만료일: " + subscription.getEndDate());
        System.out.println("메시지: 구독이 만료되었습니다. 새로운 구독을 시작하세요.");
        System.out.println("======================");
    }
}