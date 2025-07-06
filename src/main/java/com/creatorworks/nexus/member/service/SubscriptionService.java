package com.creatorworks.nexus.member.service;

import com.creatorworks.nexus.member.constant.SubscriptionStatus;
import com.creatorworks.nexus.member.dto.SubscriptionCompleteRequest;
import com.creatorworks.nexus.member.entity.Member;
import com.creatorworks.nexus.member.entity.Subscription;
import com.creatorworks.nexus.member.repository.MemberRepository;
import com.creatorworks.nexus.member.repository.SubscriptionRepository;
import com.creatorworks.nexus.notification.service.NotificationService;
import com.creatorworks.nexus.product.entity.Product;
import com.creatorworks.nexus.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final IamportService iamportService;
    private final NotificationService notificationService;
    
    /**
     * 구독 생성
     */
    @Transactional
    public Subscription createSubscription(SubscriptionCompleteRequest request, Long subscriberId) {
        // 중복 결제 방지
        if (iamportService.isDuplicatePayment(request.getMerchantUid())) {
            throw new IllegalArgumentException("이미 처리된 결제입니다.");
        }
        
        // 결제 검증
        if (!iamportService.verifyPayment(request.getImpUid(), request.getMerchantUid(), request.getAmount())) {
            throw new IllegalArgumentException("결제 검증에 실패했습니다.");
        }
        
        // 회원 조회
        Member subscriber = memberRepository.findById(subscriberId)
                .orElseThrow(() -> new IllegalArgumentException("구독자를 찾을 수 없습니다."));
        
        // 작가 조회 (상품 ID가 있으면 상품을 통해, 없으면 작가명으로 직접 조회)
        Member author;
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            author = product.getSeller();
        } else {
            // 작가명으로 직접 조회 (실제 구현에서는 작가명으로 조회하는 로직 필요)
            // 임시로 첫 번째 회원을 작가로 사용 (테스트용)
            author = memberRepository.findAll().stream()
                    .filter(member -> member.getRole().name().equals("SELLER"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("작가를 찾을 수 없습니다."));
        }
        
        // 기존 활성 구독이 있는지 확인
        Optional<Subscription> existingSubscription = subscriptionRepository
                .findBySubscriberAndAuthorAndStatus(subscriber, author, SubscriptionStatus.ACTIVE);
        
        if (existingSubscription.isPresent()) {
            // 기존 구독 연장
            Subscription subscription = existingSubscription.get();
            subscription.extend(request.getMonths());
            subscriptionRepository.save(subscription);
            
            log.info("구독 연장 완료: 구독자ID={}, 작가ID={}, 연장개월={}", 
                    subscriberId, author.getId(), request.getMonths());
            
            return subscription;
        }
        
        // 새로운 구독 생성
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusMonths(request.getMonths());
        LocalDateTime nextBillingDate = now.plusMonths(1); // 다음 달 같은 날
        
        Subscription subscription = Subscription.builder()
                .subscriber(subscriber)
                .author(author)
                .status(SubscriptionStatus.ACTIVE)
                .months(request.getMonths())
                .amount(request.getAmount())
                .startDate(now)
                .endDate(endDate)
                .nextBillingDate(nextBillingDate)
                .impUid(request.getImpUid())
                .merchantUid(request.getMerchantUid())
                .cardNumber(request.getCardNumber())
                .cardType(request.getCardType())
                .customerUid(request.getCustomerUid())
                .build();
        
        subscriptionRepository.save(subscription);
        
        // 정기결제 설정
        if (request.getCustomerUid() != null) {
            iamportService.requestRecurringPayment(request.getCustomerUid(), request.getAmount(), request.getMerchantUid());
        }
        
        log.info("구독 생성 완료: 구독자ID={}, 작가ID={}, 개월={}, 금액={}", 
                subscriberId, author.getId(), request.getMonths(), request.getAmount());
        
        return subscription;
    }
    
    /**
     * 구독 상태 확인
     */
    public boolean isSubscribed(Long subscriberId, Long authorId) {
        Member subscriber = memberRepository.findById(subscriberId).orElse(null);
        Member author = memberRepository.findById(authorId).orElse(null);
        
        if (subscriber == null || author == null) {
            return false;
        }
        
        Optional<Subscription> subscription = subscriptionRepository
                .findBySubscriberAndAuthorAndStatus(subscriber, author, SubscriptionStatus.ACTIVE);
        
        if (subscription.isPresent()) {
            Subscription sub = subscription.get();
            // 만료일 체크
            if (sub.getEndDate().isBefore(LocalDateTime.now())) {
                // 만료된 구독 비활성화
                sub.deactivate();
                subscriptionRepository.save(sub);
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 구독 해지
     */
    @Transactional
    public void cancelSubscription(Long subscriptionId, Long subscriberId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("구독을 찾을 수 없습니다."));
        
        // 구독자 본인인지 확인
        if (!subscription.getSubscriber().getId().equals(subscriberId)) {
            throw new IllegalArgumentException("구독을 해지할 권한이 없습니다.");
        }
        
        // 정기결제 취소
        if (subscription.getCustomerUid() != null) {
            iamportService.cancelRecurringPayment(subscription.getCustomerUid());
        }
        
        // 구독 상태 변경
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
        
        log.info("구독 해지 완료: 구독ID={}, 구독자ID={}", subscriptionId, subscriberId);
    }
    
    /**
     * 만료 임박 구독 조회 (7일 이내)
     */
    public List<Subscription> getExpiringSoonSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysLater = now.plusDays(7);
        
        return subscriptionRepository.findExpiringSoon(SubscriptionStatus.ACTIVE, now, sevenDaysLater);
    }
    
    /**
     * 만료된 구독 조회
     */
    public List<Subscription> getExpiredSubscriptions() {
        return subscriptionRepository.findExpired(SubscriptionStatus.ACTIVE, LocalDateTime.now());
    }
    
    /**
     * 다음 결제일 임박 구독 조회 (3일 이내)
     */
    public List<Subscription> getNextBillingSoonSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);
        
        return subscriptionRepository.findNextBillingSoon(SubscriptionStatus.ACTIVE, now, threeDaysLater);
    }
    
    /**
     * 만료된 구독 비활성화
     */
    @Transactional
    public void deactivateExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = getExpiredSubscriptions();
        
        for (Subscription subscription : expiredSubscriptions) {
            subscription.deactivate();
            subscriptionRepository.save(subscription);
            
            // 만료 알림 발송
            sendSubscriptionExpiredNotification(
                    subscription.getSubscriber().getId(),
                    subscription.getAuthor().getName()
            );
            
            log.info("만료된 구독 비활성화: 구독ID={}, 구독자ID={}, 작가ID={}", 
                    subscription.getId(), subscription.getSubscriber().getId(), subscription.getAuthor().getId());
        }
    }
    
    /**
     * 만료 임박 알림 발송
     */
    @Transactional
    public void sendExpirationNotifications() {
        List<Subscription> expiringSoonSubscriptions = getExpiringSoonSubscriptions();
        
        for (Subscription subscription : expiringSoonSubscriptions) {
            long daysUntilExpiration = subscription.getDaysUntilExpiration();
            
            // 7일, 3일, 1일 전에 알림 발송
            if (daysUntilExpiration <= 7 && daysUntilExpiration > 6) {
                sendSubscriptionExpiringNotification(
                        subscription.getSubscriber().getId(),
                        subscription.getAuthor().getName(),
                        7
                );
            } else if (daysUntilExpiration <= 3 && daysUntilExpiration > 2) {
                sendSubscriptionExpiringNotification(
                        subscription.getSubscriber().getId(),
                        subscription.getAuthor().getName(),
                        3
                );
            } else if (daysUntilExpiration <= 1 && daysUntilExpiration > 0) {
                sendSubscriptionExpiringNotification(
                        subscription.getSubscriber().getId(),
                        subscription.getAuthor().getName(),
                        1
                );
            }
        }
    }
    
    /**
     * 구독자 수 조회
     */
    public long getSubscriberCount(Long authorId) {
        return subscriptionRepository.countSubscribersByAuthorId(authorId, SubscriptionStatus.ACTIVE);
    }
    
    /**
     * 구독 개수 조회
     */
    public long getSubscriptionCount(Long subscriberId) {
        return subscriptionRepository.countActiveSubscriptionsBySubscriberId(subscriberId, SubscriptionStatus.ACTIVE);
    }
    
    // 알림 발송 메서드들 (임시 구현)
    private void sendSubscriptionExpiredNotification(Long subscriberId, String authorName) {
        log.info("구독 만료 알림 발송: 구독자ID={}, 작가명={}", subscriberId, authorName);
        // TODO: 실제 알림 발송 로직 구현
    }
    
    private void sendSubscriptionExpiringNotification(Long subscriberId, String authorName, int days) {
        log.info("구독 만료 임박 알림 발송: 구독자ID={}, 작가명={}, 남은일수={}", subscriberId, authorName, days);
        // TODO: 실제 알림 발송 로직 구현
    }
} 